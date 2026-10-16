package com.suer.vatankuyumculuk.ui.calc

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.suer.vatankuyumculuk.core.TrFormat
import com.suer.vatankuyumculuk.data.market.CalcUnit
import com.suer.vatankuyumculuk.data.market.Catalog
import com.suer.vatankuyumculuk.data.market.Catalog.fullName
import com.suer.vatankuyumculuk.data.market.MarketState
import com.suer.vatankuyumculuk.data.market.ProductGroup
import com.suer.vatankuyumculuk.ui.components.AppCard
import com.suer.vatankuyumculuk.ui.components.SourceNotice
import com.suer.vatankuyumculuk.ui.icons.AppIcons
import com.suer.vatankuyumculuk.ui.theme.AppTheme
import com.suer.vatankuyumculuk.ui.theme.TabularNumbers

@Composable
fun CalculatorScreen(
    state: MarketState,
    productCode: String,
    onProductChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val product = Catalog[productCode]?.takeIf { it.calcUnit != null } ?: Catalog[Catalog.HAS_ALTIN]!!
    val quote = state[product.code]
    var amountText by rememberSaveable { mutableStateOf("1") }
    val amount = TrFormat.parseInput(amountText)
    val unit = product.calcUnit ?: CalcUnit.GRAM
    val unitLabel = when (unit) {
        CalcUnit.GRAM -> "Gram"
        CalcUnit.PIECE -> "Adet"
        CalcUnit.CURRENCY -> product.unitHint ?: "Miktar"
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
                "Hesapla",
                style = MaterialTheme.typography.headlineMedium,
                color = c.text,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp).semantics { heading() },
            )
            Text(
                "Elinizdeki altının veya dövizin güncel değerini öğrenin.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMuted,
                modifier = Modifier.padding(start = 4.dp),
            )

            ProductPicker(product.fullName(), onSelect = onProductChange)

            OutlinedTextField(
                value = amountText,
                onValueChange = { v -> amountText = v.filter { it.isDigit() || it == ',' || it == '.' }.take(12) },
                label = { Text(unitLabel) },
                singleLine = true,
                isError = amountText.isNotBlank() && amount == null,
                supportingText = if (amountText.isNotBlank() && amount == null) {
                    { Text("Geçerli bir sayı girin, örneğin 2,5") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (unit == CalcUnit.PIECE) KeyboardType.Number else KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                textStyle = MaterialTheme.typography.headlineSmall.merge(TabularNumbers),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = c.card,
                    unfocusedContainerColor = c.card,
                    unfocusedBorderColor = c.outline,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            AppCard(Modifier.fillMaxWidth(), highlighted = true) {
                Column(Modifier.padding(20.dp)) {
                    if (quote == null) {
                        Text("Bu ürünün fiyatı şu an alınamıyor.", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                    } else {
                        ResultLine(
                            title = "Bize satarsanız",
                            subtitle = "Alış fiyatı ile",
                            value = if (amount != null && quote.buy != null) amount * quote.buy else null,
                        )
                        HorizontalDivider(color = c.outline, modifier = Modifier.padding(vertical = 14.dp))
                        ResultLine(
                            title = "Bizden alırsanız",
                            subtitle = "Satış fiyatı ile",
                            value = if (amount != null && quote.sell != null) amount * quote.sell else null,
                        )
                        Spacer(Modifier.height(12.dp))
                        val unitWord = when (unit) {
                            CalcUnit.GRAM -> "gram"
                            CalcUnit.PIECE -> "adet"
                            CalcUnit.CURRENCY -> "birim"
                        }
                        Text(
                            "1 $unitWord: alış ${quote.buy?.let { TrFormat.number(it, product.decimals) } ?: "—"} ₺ · " +
                                "satış ${quote.sell?.let { TrFormat.number(it, product.decimals) } ?: "—"} ₺",
                            style = MaterialTheme.typography.bodySmall.merge(TabularNumbers),
                            color = c.textMuted,
                        )
                    }
                }
            }
            if (product.group == ProductGroup.GOLD && unit == CalcUnit.GRAM) {
                Text(
                    "İşçilik ücreti hesaba dahil değildir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            SourceNotice(Modifier.padding(horizontal = 4.dp))
        }
    }
}

@Composable
private fun ResultLine(title: String, subtitle: String, value: Double?) {
    val c = AppTheme.colors
    val text = value?.let { TrFormat.number(it, 2) } ?: "—"
    Row(
        Modifier.semantics(mergeDescendants = true) { contentDescription = "$title $text lira" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.text)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        }
        Text(text, style = MaterialTheme.typography.headlineSmall.merge(TabularNumbers), color = c.text)
        Text(" ₺", style = MaterialTheme.typography.titleMedium, color = c.textMuted)
    }
}

@Composable
private fun ProductPicker(selectedName: String, onSelect: (String) -> Unit) {
    val c = AppTheme.colors
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(c.card)
                .clickable(role = Role.DropdownList, onClickLabel = "Ürün seç") { open = true }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Ürün", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                Text(selectedName, style = MaterialTheme.typography.titleMedium, color = c.text)
            }
            Icon(AppIcons.KeyboardArrowDown, contentDescription = null, tint = c.textMuted)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.heightIn(max = 420.dp)) {
            var lastGroup: ProductGroup? = null
            Catalog.calculable.forEach { p ->
                if (p.group != lastGroup) {
                    lastGroup = p.group
                    Text(
                        p.group.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMuted,
                        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 4.dp),
                    )
                }
                DropdownMenuItem(
                    text = { Text(p.fullName()) },
                    onClick = {
                        onSelect(p.code)
                        open = false
                    },
                    trailingIcon = if (p.fullName() == selectedName) {
                        { Icon(AppIcons.Check, contentDescription = "Seçili", tint = c.goldInk, modifier = Modifier.size(20.dp)) }
                    } else null,
                )
            }
        }
    }
}
