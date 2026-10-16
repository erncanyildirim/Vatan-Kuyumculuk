package com.suer.vatankuyumculuk.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.suer.vatankuyumculuk.AppLinks
import com.suer.vatankuyumculuk.data.ThemeMode
import com.suer.vatankuyumculuk.ui.components.AppCard
import com.suer.vatankuyumculuk.ui.icons.AppIcons
import com.suer.vatankuyumculuk.ui.theme.AppTheme

const val APP_VERSION = "1.0"

@Composable
fun SettingsScreen(
    theme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onShareApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val uri = LocalUriHandler.current
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
            Text(
                "Ayarlar",
                style = MaterialTheme.typography.headlineMedium,
                color = c.text,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp).semantics { heading() },
            )

            Label("Görünüm")
            AppCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeOption("Sistem", AppIcons.BrightnessAuto, theme == ThemeMode.SYSTEM, Modifier.weight(1f)) { onThemeChange(ThemeMode.SYSTEM) }
                    ThemeOption("Koyu", AppIcons.DarkMode, theme == ThemeMode.DARK, Modifier.weight(1f)) { onThemeChange(ThemeMode.DARK) }
                    ThemeOption("Açık", AppIcons.LightMode, theme == ThemeMode.LIGHT, Modifier.weight(1f)) { onThemeChange(ThemeMode.LIGHT) }
                }
            }

            Label("Diğer")
            AppCard(Modifier.fillMaxWidth()) {
                Column {
                    LinkRow(AppIcons.Share, "Uygulamayı paylaş", onClick = onShareApp)
                    HorizontalDivider(color = c.outline)
                    LinkRow(AppIcons.PrivacyTip, "Gizlilik politikası") { openSafely(AppLinks.privacyUrl()) }
                    HorizontalDivider(color = c.outline)
                    LinkRow(AppIcons.Info, "Destek ve iletişim") { openSafely(AppLinks.supportUrl()) }
                    HorizontalDivider(color = c.outline)
                    LinkRow(AppIcons.Public, "Web sitesi") { openSafely(AppLinks.SITE_URL) }
                }
            }

            Label("Bilgilendirme")
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Fiyatlar Harem Altın piyasa verilerinden alınır ve yalnızca bilgi amaçlıdır; yatırım tavsiyesi değildir. " +
                            "Mağazadaki satış fiyatı, işçilik ve stok durumuna göre farklılık gösterebilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted,
                    )
                    Text(
                        "Uygulama üyelik istemez, kişisel veri toplamaz. Favorileriniz ve tema tercihiniz yalnızca bu cihazda saklanır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted,
                    )
                }
            }

            Text(
                "Sürüm $APP_VERSION",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = AppTheme.colors.textMuted,
        modifier = Modifier.padding(start = 6.dp, top = 6.dp),
    )
}

@Composable
private fun ThemeOption(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) c.gold else c.card)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) c.onGold else c.textMuted, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) c.onGold else c.text)
    }
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = c.goldInk, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = c.text, modifier = Modifier.weight(1f))
        Icon(AppIcons.ChevronRight, contentDescription = null, tint = c.textMuted)
    }
}
