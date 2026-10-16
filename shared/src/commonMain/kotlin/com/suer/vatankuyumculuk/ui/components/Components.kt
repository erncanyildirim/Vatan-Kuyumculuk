package com.suer.vatankuyumculuk.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suer.vatankuyumculuk.core.TrFormat
import com.suer.vatankuyumculuk.core.TrTime
import com.suer.vatankuyumculuk.data.market.ConnectionStatus
import com.suer.vatankuyumculuk.data.market.Tick
import com.suer.vatankuyumculuk.resources.Res
import com.suer.vatankuyumculuk.resources.logo_mark
import com.suer.vatankuyumculuk.ui.icons.AppIcons
import com.suer.vatankuyumculuk.ui.theme.AppTheme
import com.suer.vatankuyumculuk.ui.theme.BrandNavy
import com.suer.vatankuyumculuk.ui.theme.TabularNumbers
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/** The gold Vatan mark; on light backgrounds it sits on a navy tile for contrast. */
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    val image = @Composable { m: Modifier ->
        Image(
            painter = painterResource(Res.drawable.logo_mark),
            contentDescription = "Vatan Kuyumculuk logosu",
            modifier = m,
        )
    }
    if (AppTheme.colors.isDark) {
        image(modifier)
    } else {
        Box(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(BrandNavy)
                .padding(horizontal = 6.dp, vertical = 5.dp),
        ) { image(modifier) }
    }
}

/** Current device time, refreshed every [periodMs]. */
@Composable
fun rememberNow(periodMs: Long = 1_000): Long {
    var now by remember { mutableLongStateOf(TrTime.nowMillis()) }
    LaunchedEffect(periodMs) {
        while (true) {
            delay(periodMs)
            now = TrTime.nowMillis()
        }
    }
    return now
}

/** Small pill showing whether prices are live. */
@Composable
fun ConnectionPill(status: ConnectionStatus, marketQuiet: Boolean, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val (label, color) = when {
        status == ConnectionStatus.LIVE && marketQuiet -> "Piyasa kapalı" to c.textMuted
        status == ConnectionStatus.LIVE -> "Canlı" to c.up
        status == ConnectionStatus.CONNECTING -> "Bağlanıyor" to c.warning
        status == ConnectionStatus.RECONNECTING -> "Yeniden bağlanıyor" to c.warning
        status == ConnectionStatus.PAUSED -> "Mağaza fiyatları" to c.goldInk
        else -> "Çevrimdışı" to c.down
    }
    val pulsing = status == ConnectionStatus.LIVE && !marketQuiet
    val transition = rememberInfiniteTransition()
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
    )
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .semantics { contentDescription = "Bağlantı durumu: $label" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .alpha(if (pulsing) pulse else 1f)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/** A price that briefly flashes green/red when it changes. */
@Composable
fun FlashingPrice(
    text: String,
    tick: Tick,
    seq: Long,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.text,
) {
    val c = AppTheme.colors
    var flash by remember { mutableStateOf(false) }
    var lastSeq by remember { mutableLongStateOf(seq) }
    LaunchedEffect(seq) {
        if (seq != lastSeq) {
            lastSeq = seq
            flash = true
            delay(450)
            flash = false
        }
    }
    val target = when {
        !flash -> Color.Transparent
        tick == Tick.UP -> c.upSoft
        tick == Tick.DOWN -> c.downSoft
        else -> Color.Transparent
    }
    val bg by animateColorAsState(target, tween(if (flash) 120 else 600))
    val fg by animateColorAsState(
        when {
            flash && tick == Tick.UP -> c.up
            flash && tick == Tick.DOWN -> c.down
            else -> color
        },
        tween(if (flash) 120 else 600),
    )
    Text(
        text = text,
        style = style.merge(TabularNumbers),
        color = fg,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 4.dp, vertical = 1.dp),
    )
}

/** Daily change, e.g. "▲ %1,24". */
@Composable
fun ChangeChip(changePct: Double?, modifier: Modifier = Modifier, compact: Boolean = false) {
    if (changePct == null) return
    val c = AppTheme.colors
    val up = changePct > 0.0049
    val down = changePct < -0.0049
    val color = when {
        up -> c.up
        down -> c.down
        else -> c.textMuted
    }
    val icon = when {
        up -> AppIcons.ArrowDropUp
        down -> AppIcons.ArrowDropDown
        else -> AppIcons.Remove
    }
    val spoken = when {
        up -> "yüzde ${TrFormat.number(changePct, 2)} artış"
        down -> "yüzde ${TrFormat.number(-changePct, 2)} düşüş"
        else -> "değişim yok"
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.13f))
            .padding(start = 2.dp, end = 7.dp, top = 2.dp, bottom = 2.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Günlük değişim $spoken" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(if (compact) 16.dp else 20.dp))
        Text(
            TrFormat.percentAbs(changePct),
            style = (if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium).merge(TabularNumbers),
            color = color,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.text,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** Rounded card surface used across the app. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable () -> Unit,
) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(c.card)
            .border(1.dp, if (highlighted) c.gold.copy(alpha = 0.55f) else c.outline, MaterialTheme.shapes.large),
    ) { content() }
}

/** Source attribution and the "not investment advice" notice required on every price screen. */
@Composable
fun SourceNotice(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Fiyat kaynağı: Harem Altın. Mağaza fiyatları farklılık gösterebilir.",
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.textMuted,
        )
        Text(
            "Fiyatlar bilgi amaçlıdır, yatırım tavsiyesi değildir. Kesin fiyat için mağazamızı arayın.",
            style = MaterialTheme.typography.bodySmall,
            color = AppTheme.colors.textMuted,
        )
    }
}

/** "TL" or other currency marker shown next to a price. */
@Composable
fun CurrencyLabel(symbol: String, modifier: Modifier = Modifier) {
    if (symbol.isEmpty()) return
    Text(
        symbol,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = AppTheme.colors.textMuted,
        modifier = modifier,
    )
}
