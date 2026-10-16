package com.suer.vatankuyumculuk.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.suer.vatankuyumculuk.ExternalLinks
import com.suer.vatankuyumculuk.core.TrTime
import com.suer.vatankuyumculuk.data.config.StoreInfo
import com.suer.vatankuyumculuk.ui.components.AppCard
import com.suer.vatankuyumculuk.ui.components.BrandMark
import com.suer.vatankuyumculuk.ui.components.rememberNow
import com.suer.vatankuyumculuk.ui.icons.AppIcons
import com.suer.vatankuyumculuk.ui.theme.AppTheme

@Composable
fun StoreScreen(store: StoreInfo, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val uri = LocalUriHandler.current
    val now = rememberNow(30_000)
    val open = isOpen(store, now)
    val mapsQuery = store.mapsQuery.ifBlank { "${store.name} ${store.city}" }
    fun openSafely(url: String) {
        runCatching { uri.openUri(url) }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BrandMark(Modifier.height(64.dp))
                    Spacer(Modifier.height(14.dp))
                    Text(
                        store.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = c.text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(store.city, style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                    if (open != null) {
                        Spacer(Modifier.height(10.dp))
                        OpenBadge(open)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (store.phone.isNotBlank()) {
                    ActionButton("Ara", AppIcons.Call, c.gold, c.onGold, Modifier.weight(1f)) {
                        openSafely(ExternalLinks.phone(store.phone))
                    }
                }
                if (store.whatsapp.isNotBlank()) {
                    ActionButton("WhatsApp", AppIcons.Chat, Color(0xFF25D366), Color(0xFF06301A), Modifier.weight(1f)) {
                        openSafely(ExternalLinks.whatsapp(store.whatsapp, "Merhaba, fiyat bilgisi almak istiyorum."))
                    }
                }
                if (store.address.isNotBlank() || (store.lat != null && store.lng != null)) {
                    ActionButton("Yol tarifi", AppIcons.Directions, c.cardHigh, c.text, Modifier.weight(1f)) {
                        openSafely(ExternalLinks.maps(mapsQuery, store.lat, store.lng))
                    }
                }
            }

            AppCard(Modifier.fillMaxWidth()) {
                Column {
                    if (store.address.isNotBlank()) {
                        InfoRow(AppIcons.Place, "Adres", store.address) {
                            openSafely(ExternalLinks.maps(mapsQuery, store.lat, store.lng))
                        }
                        HorizontalDivider(color = c.outline)
                    }
                    if (store.hoursText.isNotBlank()) {
                        InfoRow(AppIcons.Schedule, "Çalışma saatleri", store.hoursText)
                    }
                    if (store.phone.isNotBlank()) {
                        if (store.address.isNotBlank() || store.hoursText.isNotBlank()) HorizontalDivider(color = c.outline)
                        InfoRow(AppIcons.Call, "Telefon", formatPhone(store.phone)) {
                            openSafely(ExternalLinks.phone(store.phone))
                        }
                    }
                    if (store.instagram.isNotBlank()) {
                        HorizontalDivider(color = c.outline)
                        InfoRow(AppIcons.PhotoCamera, "Instagram", "@" + store.instagram.trim().removePrefix("@").substringAfterLast('/')) {
                            openSafely(ExternalLinks.instagram(store.instagram))
                        }
                    }
                    if (store.email.isNotBlank()) {
                        HorizontalDivider(color = c.outline)
                        InfoRow(AppIcons.Mail, "E-posta", store.email) { openSafely("mailto:${store.email}") }
                    }
                }
            }
        }
    }
}

/** Null when no opening hours are configured. */
private fun isOpen(store: StoreInfo, now: Long): Boolean? {
    if (store.openingHours.isEmpty()) return null
    val p = TrTime.parts(now)
    return store.openingHours.any { h ->
        val open = TrTime.parseHourMinute(h.open) ?: return@any false
        val close = TrTime.parseHourMinute(h.close) ?: return@any false
        p.dayOfWeek in h.days && p.minuteOfDay in open until close
    }
}

/** "+905054348592" -> "0505 434 85 92" */
private fun formatPhone(raw: String): String {
    val d = raw.filter { it.isDigit() }.let { if (it.startsWith("90") && it.length == 12) "0" + it.drop(2) else it }
    if (d.length != 11) return raw
    return "${d.substring(0, 4)} ${d.substring(4, 7)} ${d.substring(7, 9)} ${d.substring(9)}"
}

@Composable
private fun OpenBadge(open: Boolean) {
    val c = AppTheme.colors
    val color = if (open) c.up else c.down
    Row(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(if (open) "Şu an açık" else "Şu an kapalı", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .heightIn(min = 76.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, value: String, onClick: (() -> Unit)? = null) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = c.goldInk, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = c.text)
        }
        if (onClick != null) Icon(AppIcons.ChevronRight, contentDescription = null, tint = c.textMuted)
    }
}
