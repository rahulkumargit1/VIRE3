package com.example.payoffline.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.payoffline.data.model.UiState
import com.example.payoffline.ui.components.*
import com.example.payoffline.ui.theme.Emerald600
import com.example.payoffline.viewmodel.UssdViewModel

@Composable
fun BalanceScreen(vm: UssdViewModel) {
    val balanceUiState by vm.balanceUiState.collectAsState()
    val selectedSim    by vm.selectedSim.collectAsState()
    val sims           by vm.sims.collectAsState()
    val scrollState    = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GradientHeaderCard(
            title    = "Balance & Services",
            subtitle = "Enquiry via *99#",
            icon     = Icons.Filled.AccountBalance
        )

        // SIM info
        if (selectedSim != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SIM Card", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                SimSelectorChip(sim = selectedSim, onClick = { vm.setShowSimSelector(true) })
            }
        }

        // Important notice — link bank first
        Surface(
            color = Color(0xFFFFF7ED),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WarningAmber, null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Before using these services", style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF92400E))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Make sure your mobile number is linked to a bank account. " +
                    "If you see 'Bank not found', tap Link Bank Account first and follow the steps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF92400E)
                )
            }
        }

        // How it works
        InfoCard(
            message = "ℹ️ Each button opens your phone dialer with the correct *99# code pre-filled. " +
                "Simply tap the Call button and follow your carrier's on-screen instructions. " +
                "No internet required.",
            icon = Icons.Filled.HelpOutline
        )

        StatusBanner(uiState = balanceUiState, onDismiss = vm::clearBalanceResponse)

        // Action buttons
        Text("Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        // Link Bank — most important first if not set up
        Button(
            onClick  = { vm.linkBankAccount() },
            enabled  = balanceUiState !is UiState.Loading && sims.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
        ) {
            if (balanceUiState is UiState.Loading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Opening…")
            } else {
                Icon(Icons.Filled.AddLink, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Link Bank Account (*99*2#)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }

        // Check Balance
        Button(
            onClick  = { vm.checkBalance() },
            enabled  = balanceUiState !is UiState.Loading && sims.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Emerald600)
        ) {
            Icon(Icons.Filled.AccountBalance, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Check Balance (*99*3#)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        // Mini Statement
        OutlinedButton(
            onClick  = { vm.miniStatement() },
            enabled  = balanceUiState !is UiState.Loading && sims.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Receipt, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Mini Statement (*99*4#)", style = MaterialTheme.typography.titleSmall)
        }

        // Change PIN
        OutlinedButton(
            onClick  = { vm.changePin() },
            enabled  = balanceUiState !is UiState.Loading && sims.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Lock, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Change UPI PIN (*99*5#)", style = MaterialTheme.typography.titleSmall)
        }

        // Open main menu
        TextButton(
            onClick  = { vm.openDialerFallback("base") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Filled.Dialpad, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Open *99# Main Menu")
        }

        Spacer(Modifier.height(80.dp))
    }
}
