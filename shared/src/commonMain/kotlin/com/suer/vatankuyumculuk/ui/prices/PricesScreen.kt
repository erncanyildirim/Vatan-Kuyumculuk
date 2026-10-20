package com.suer.vatankuyumculuk.ui.prices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.suer.vatankuyumculuk.core.TrFormat
import com.suer.vatankuyumculuk.core.TrTime
import com.suer.vatankuyumculuk.data.market.Catalog
import com.suer.vatankuyumculuk.data.market.ConnectionStatus
import com.suer.vatankuyumculuk.data.market.MarketState
import com.suer.vatankuyumculuk.data.market.PriceSource
import com.suer.vatankuyumculuk.data.market.PricedQuote
import com.suer.vatankuyumculuk.data.market.ProductGroup
import com.suer.vatankuyumculuk.ui.components.AppCard
import com.suer.vatankuyumculuk.ui.components.BrandMark
import com.suer.vatankuyumculuk.ui.components.ChangeChip
import com.suer.vatankuyumculuk.ui.components.ConnectionPill
import com.suer.vatankuyumculuk.ui.components.CurrencyLabel
import com.suer.vatankuyumculuk.ui.components.FlashingPrice
import com.suer.vatankuyumculuk.ui.components.SourceNotice
import com.suer.vatankuyumculuk.ui.components.rememberNow
import com.suer.vatankuyumculuk.ui.icons.AppIcons
import com.suer.vatankuyumculuk.ui.theme.AppTheme
import com.suer.vatankuyumculuk.ui.theme.TabularNumbers
import kotlinx.coroutines.delay

/** Tabs on the price screen; [group] is null for favorites. */
private enum class PriceTab(val title: String, val group: ProductGroup?) {
    FAVORITES("Favoriler", null),
    GOLD("Altın", ProductGroup.GOLD),
    COINS("Sarrafiye", ProductGroup.COINS),
    CURRENCY("Döviz", ProductGroup.CURRENCY),
    PARITY("Parite", ProductGroup.PARITY),
    METALS("Emtia", ProductGroup.METALS),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricesScreen(
    state: MarketState,
    favorites: List<String>,
    onToggleFavorite: (String) -> Unit,
    onRefresh: () -> Unit,
    initialTab: String? = null,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(PriceTab.entries.find { it.name == initialTab } ?: PriceTab.FAVORITES) }
    var refreshing by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(1_200)
            refreshing = false
        }
    }
    val now = rememberNow()
    val quiet = state.isMarketQuiet(now)
    val rows: List<PricedQuote> = when (val g = tab.group) {
        null -> favorites.mapNotNull { state[it] }
        else -> state.inGroup(g)
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            refreshing = true
            onRefresh()
        },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "header") {
                Header(state, quiet, Modifier.contentWidth().padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 8.dp))
            }
            if (state.announcement.enabled && state.announcement.text.isNotBlank()) {
                item(key = "announcement") {
                    AnnouncementBanner(
                        state.announcement.text,
                        warning = state.announcement.style == "warning",
                        modifier = Modifier.contentWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
            item(key = "hero") {
                HeroCard(
                    quote = state[Catalog.HAS_ALTIN],
                    state = state,
                    now = now,
                    onRetry = onRefresh,
                    modifier = Modifier.contentWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            item(key = "tabs") {
                TabsRow(tab, onSelect = { tab = it }, modifier = Modifier.contentWidth().padding(top = 8.dp, bottom = 4.dp))
            }
            item(key = "columns") {
                ColumnHeader(Modifier.contentWidth().padding(horizontal = 20.dp, vertical = 6.dp))
            }
            if (rows.isEmpty()) {
                item(key = "empty") {
                    EmptyRows(
                        favoritesTab = tab == PriceTab.FAVORITES,
                        loading = !state.hasData,
                        modifier = Modifier.contentWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                    )
                }
            } else {
                items(rows, key = { it.code }) { q ->
                    Column(Modifier.contentWidth().padding(horizontal = 16.dp)) {
                        PriceRow(
                            quote = q,
                            favorite = q.code in favorites,
                            onToggleFavorite = { onToggleFavorite(q.code) },
                        )
                        HorizontalDivider(color = AppTheme.colors.outline.copy(alpha = 0.6f))
                    }
                }
            }
            item(key = "notice") {
                SourceNotice(Modifier.contentWidth().padding(horizontal = 20.dp, vertical = 16.dp))
            }
        }
    }
}

private fun Modifier.contentWidth(): Modifier = this.widthIn(max = 720.dp).fillMaxWidth()

@Composable
private fun Header(state: MarketState, quiet: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        BrandMark(Modifier.height(52.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                state.store.name,
                style = MaterialTheme.typography.titleLarge,
                color = AppTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                "${state.store.city} · Canlı altın ve döviz",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textMuted,
                maxLines = 1,
            )
        }
        ConnectionPill(state.connection, quiet)
    }
}

@Composable
private fun AnnouncementBanner(text: String, warning: Boolean, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val tint = if (warning) c.warning else c.goldInk
    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (warning) c.warningSoft else c.goldSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcons.Campaign, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.text)
    }
}

@Composable
private fun HeroCard(
    quote: PricedQuote?,
    state: MarketState,
    now: Long,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    AppCard(modifier, highlighted = true) {
        Column(
            Modifier
                .background(c.gold.copy(alpha = if (c.isDark) 0.05f else 0.08f))
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "HAS ALTIN",
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing),
                    color = c.goldInk,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.width(8.dp))
                Text("gram · 24 ayar", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                Spacer(Modifier.weight(1f))
                if (quote != null) ChangeChip(quote.changePct)
            }
            Spacer(Modifier.height(14.dp))
            if (quote == null) {
                Text(
                    if (state.connection == ConnectionStatus.OFFLINE) "Fiyatlara ulaşılamadı" else "Fiyatlar yükleniyor…",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textMuted,
                )
                if (state.connection == ConnectionStatus.OFFLINE) {
                    TextButton(onClick = onRetry) { Text("Tekrar dene") }
                }
                return@Column
            }
            Row(Modifier.fillMaxWidth()) {
                HeroPrice("Alış", quote.buy, quote, isSell = false, Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                HeroPrice("Satış", quote.sell, quote, isSell = true, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = c.outline)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val low = quote.low
                val high = quote.high
                if (low != null && high != null && high > low) {
                    Text(
                        "Gün içi  ${TrFormat.number(low, 2)} – ${TrFormat.number(high, 2)}",
                        style = MaterialTheme.typography.bodySmall.merge(TabularNumbers),
                        color = c.textMuted,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Text(
                    updatedText(quote, state, now),
                    style = MaterialTheme.typography.bodySmall.merge(TabularNumbers),
                    color = c.textMuted,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

private fun updatedText(quote: PricedQuote, state: MarketState, now: Long): String {
    if (quote.source == PriceSource.FIXED) return "Mağaza fiyatı"
    if (state.fromCache) return "Son kayıtlı fiyat"
    val t = quote.updatedAtMillis ?: state.lastMessageAtMillis ?: return ""
    val diff = (now - t).coerceAtLeast(0)
    if (diff < 60_000) return "Güncel · ${TrTime.hhmmss(t)}"
    val then = TrTime.parts(t)
    val today = TrTime.parts(now)
    val sameDay = then.year == today.year && then.month == today.month && then.day == today.day
    return if (sameDay) "Son güncelleme ${TrTime.hhmm(t)}" else "Son güncelleme ${TrTime.dayMonthTime(t)}"
}

@Composable
private fun HeroPrice(label: String, value: Double?, quote: PricedQuote, isSell: Boolean, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val text = value?.let { TrFormat.number(it, quote.product.decimals) } ?: "—"
    Column(
        modifier.semantics(mergeDescendants = true) { contentDescription = "Has altın $label $text lira" },
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            FlashingPrice(
                text = text,
                tick = if (isSell) quote.sellTick else quote.buyTick,
                seq = quote.tickSeq,
                style = MaterialTheme.typography.headlineLarge,
            )
            Text("₺", style = MaterialTheme.typography.titleMedium, color = c.textMuted, modifier = Modifier.padding(bottom = 3.dp))
        }
    }
}

@Composable
private fun TabsRow(selected: PriceTab, onSelect: (PriceTab) -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PriceTab.entries.forEach { t ->
            val isSelected = t == selected
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) c.gold else c.card)
                    .clickable(role = Role.Tab) { onSelect(t) }
                    .semantics { this.selected = isSelected }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            ) {
                Text(
                    t.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) c.onGold else c.text,
                )
            }
        }
    }
}

@Composable
private fun ColumnHeader(modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.labelSmall
    val color = AppTheme.colors.textMuted
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("ÜRÜN", style = style, color = color, modifier = Modifier.weight(1.35f))
        Text("ALIŞ", style = style, color = color, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("SATIŞ", style = style, color = color, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(36.dp))
    }
}

@Composable
private fun PriceRow(quote: PricedQuote, favorite: Boolean, onToggleFavorite: () -> Unit) {
    val c = AppTheme.colors
    val p = quote.product
    val buy = quote.buy?.let { TrFormat.number(it, p.decimals) } ?: "—"
    val sell = quote.sell?.let { TrFormat.number(it, p.decimals) } ?: "—"
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .weight(1.35f)
                .semantics(mergeDescendants = true) {
                    contentDescription = "${p.spokenName}, alış $buy, satış $sell"
                },
        ) {
            Text(
                p.name,
                style = MaterialTheme.typography.titleSmall,
                color = c.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (p.variant != null) {
                    VariantBadge(p.variant)
                    Spacer(Modifier.width(6.dp))
                }
                val hint = when {
                    quote.source == PriceSource.FIXED -> "Mağaza"
                    p.variant == null -> p.unitHint
                    else -> null
                }
                if (hint != null) {
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                ChangeChip(quote.changePct, compact = true)
            }
        }
        PriceCell(buy, quote, isSell = false, Modifier.weight(1f))
        PriceCell(sell, quote, isSell = true, Modifier.weight(1f))
        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
            Icon(
                if (favorite) AppIcons.Star else AppIcons.StarBorder,
                contentDescription = if (favorite) "${p.spokenName} favorilerden çıkar" else "${p.spokenName} favorilere ekle",
                tint = if (favorite) c.goldInk else c.textMuted,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun PriceCell(text: String, quote: PricedQuote, isSell: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        FlashingPrice(
            text = text,
            tick = if (isSell) quote.sellTick else quote.buyTick,
            seq = quote.tickSeq,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (isSell) FontWeight.Bold else FontWeight.SemiBold),
        )
        if (quote.product.currency != "₺") CurrencyLabel(quote.product.currency)
    }
}

@Composable
private fun VariantBadge(text: String) {
    val c = AppTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = c.goldInk,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(c.goldSoft)
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}

@Composable
private fun EmptyRows(favoritesTab: Boolean, loading: Boolean, modifier: Modifier = Modifier) {
    val text = when {
        loading -> "Fiyatlar yükleniyor…"
        favoritesTab -> "Henüz favoriniz yok. Diğer sekmelerde yıldıza dokunarak ekleyebilirsiniz."
        else -> "Bu kategoride gösterilecek fiyat yok."
    }
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.textMuted,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
