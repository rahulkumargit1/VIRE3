package com.example.payoffline.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.payoffline.data.model.UiState
import com.example.payoffline.ui.components.*
import com.example.payoffline.ui.theme.Violet600
import com.example.payoffline.viewmodel.UssdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayScreen(
    vm: UssdViewModel,
    onRequestPermission: () -> Unit,
    hasCallPermission: Boolean
) {
    val recipient       by vm.recipient.collectAsState()
    val amount          by vm.amount.collectAsState()
    val pin             by vm.pin.collectAsState()
    val uiState         by vm.uiState.collectAsState()
    val selectedSim     by vm.selectedSim.collectAsState()
    val sims            by vm.sims.collectAsState()
    val settings        by vm.settings.collectAsState()
    val showSimSelector by vm.showSimSelector.collectAsState()

    val focusManager = LocalFocusManager.current
    val amountFocus  = remember { FocusRequester() }
    val scrollState  = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GradientHeaderCard(
            title    = "Send Money",
            subtitle = "Offline UPI via *99#",
            icon     = Icons.Filled.Send
        )

        if (!hasCallPermission) {
            PermissionBanner(onRequest = onRequestPermission)
        }

        // SIM selector
        if (sims.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SIM Card", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                SimSelectorChip(sim = selectedSim, onClick = { vm.setShowSimSelector(true) })
            }
        } else {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SimCardAlert, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("No SIM detected", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        // Recent recipients
        if (settings.savedRecipients.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recent", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(settings.savedRecipients.take(5)) { r ->
                        FilterChip(
                            selected = recipient == r,
                            onClick  = { vm.onRecipientChange(r) },
                            label    = { Text(r, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(Icons.Filled.Person, null, Modifier.size(14.dp)) }
                        )
                    }
                }
            }
        }

        // Form
        Surface(shape = RoundedCornerShape(20.dp), tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PayTextField(
                    value         = recipient,
                    onValueChange = vm::onRecipientChange,
                    label         = "Recipient Mobile Number",
                    placeholder   = "e.g. 9876543210",
                    leadingIcon   = Icons.Filled.Person,
                    keyboardType  = KeyboardType.Phone,
                    imeAction     = ImeAction.Next,
                    onImeAction   = { amountFocus.requestFocus() }
                )
                PayTextField(
                    value         = amount,
                    onValueChange = { if (it.length <= 7 && it.all { c -> c.isDigit() }) vm.onAmountChange(it) },
                    label         = "Amount (₹)",
                    placeholder   = "e.g. 500",
                    leadingIcon   = Icons.Filled.CurrencyRupee,
                    keyboardType  = KeyboardType.Number,
                    imeAction     = ImeAction.Done,
                    onImeAction   = { focusManager.clearFocus() },
                    modifier      = Modifier.focusRequester(amountFocus)
                )
            }
        }

        // How it works info
        InfoCard(
            message = "ℹ️ How it works: Tapping Pay Now opens your phone's dialer with *99# pre-filled. " +
                "Tap the Call button, then follow the menu:\n" +
                "• Select 1 for Send Money\n" +
                "• Enter recipient number, amount, and your UPI PIN when prompted",
            icon = Icons.Filled.HelpOutline
        )

        StatusBanner(uiState = uiState, onDismiss = vm::clearResponse)

        Button(
            onClick = {
                focusManager.clearFocus()
                vm.sendMoney()
            },
            enabled  = uiState !is UiState.Loading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Violet600)
        ) {
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Opening…")
            } else {
                Icon(Icons.Filled.Send, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Pay Now — Open Dialer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        if (recipient.isNotBlank() || amount.isNotBlank()) {
            TextButton(onClick = vm::clearForm, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.Filled.Clear, null, Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clear Form")
            }
        }

        Spacer(Modifier.height(80.dp))
    }

    // SIM Selector bottom sheet
    if (showSimSelector) {
        ModalBottomSheet(onDismissRequest = { vm.setShowSimSelector(false) }) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select SIM", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                sims.forEach { sim ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { vm.selectSim(sim) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedSim?.subscriptionId == sim.subscriptionId)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SimCard, null,
                                tint = if (selectedSim?.subscriptionId == sim.subscriptionId)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(sim.displayName, style = MaterialTheme.typography.titleSmall)
                                Text(sim.carrierName, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                sim.number?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (selectedSim?.subscriptionId == sim.subscriptionId) {
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
