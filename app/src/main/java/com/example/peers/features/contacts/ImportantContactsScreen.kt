package com.example.peers.features.contacts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peers.features.dashboard.DashboardUser
import com.example.peers.features.dashboard.components.PeersSubScreenHeader
import com.example.peers.ui.theme.GoogleSans
import com.example.peers.ui.theme.Lato
import com.example.peers.ui.theme.PeersLavender
import com.example.peers.ui.theme.PeersMint
import com.example.peers.ui.theme.PeersPrimaryIndigo
import com.example.peers.ui.theme.PeersSky

enum class ContactCategory(val label: String) {
    ALL("All"),
    HOSPITAL("Hospital"),
    SAFETY("Safety & Women"),
    WELFARE("Welfare & DSW"),
    HOSTELS("Hostels"),
    NATIONAL("National")
}

private data class EmergencyContact(
    val id: String,
    val title: String,
    val category: ContactCategory,
    val primaryPhone: String,
    val secondaryPhone: String = "",
    val detail: String = "",
    val timingOrLocation: String = "",
    val accent: Color = PeersSky,
)

private val officialContacts = listOf(
    // ── Hospital & Medical (LPU Emergency Numbers) ─────────────────────────────
    EmergencyContact("h1", "Hospital Reception 1", ContactCategory.HOSPITAL, "01824-444079", "01824-501227", "Extension: 4079 · Uni Hospital Reception", "24x7 Service", PeersLavender),
    EmergencyContact("h2", "Hospital Enquiry", ContactCategory.HOSPITAL, "+91-9878426880", "+91-9815364977", "General medical enquiry & ambulance desk", "24x7 Service", PeersLavender),
    EmergencyContact("h3", "Hospital Chemist Shop", ContactCategory.HOSPITAL, "01824-444068", "", "Extension: 4068 · Campus pharmacy", "Uni Hospital", PeersLavender),
    EmergencyContact("h4", "Hospital Male Ward", ContactCategory.HOSPITAL, "01824-444066", "9914550656", "Extension: 4066 · Attendant: Mr. Lovepreet Singh", "24x7 Ward", PeersLavender),
    EmergencyContact("h5", "Hospital Female Ward", ContactCategory.HOSPITAL, "01824-444067", "9592571889", "Extension: 4067 · Staff: Mandeep Kaur, Ms. Rajpinder (6284518196)", "24x7 Ward", PeersLavender),
    EmergencyContact("h6", "Medical Laboratory", ContactCategory.HOSPITAL, "01824-444069", "", "Extension: 4069 · Diagnostics & pathology", "Uni Hospital", PeersLavender),
    EmergencyContact("h7", "Dr. Anil Malhotra", ContactCategory.HOSPITAL, "9815364977", "01824-444075", "University Hospital Medical Officer", "24x7 On-Call", PeersLavender),
    EmergencyContact("h8", "Dr. Vijay Mohan Soni", ContactCategory.HOSPITAL, "9878426880", "01824-444073", "University Hospital Medical Officer", "24x7 On-Call", PeersLavender),

    // ── Women Help Center, Safety Cell, Fire & Anti-Ragging ───────────────────
    EmergencyContact("s1", "Women Help Center — Dr. Monica Gulati", ContactCategory.SAFETY, "9915020408", "01824-444040", "Women Help Center and Safety Cell", "9:00 AM – 5:00 PM", Color(0xFFFFD9E2)),
    EmergencyContact("s2", "Women Safety & Anti-Ragging — Mr. Surinder Khurana", ContactCategory.SAFETY, "9876644331", "01824-444097", "Women Safety Cell, Fire & Safety, Anti-Ragging Committee (ARC)", "24x7 Safety / 9 AM–5 PM ARC", Color(0xFFFFD9E2)),
    EmergencyContact("s3", "Women Help Center — Ms. Madhvi", ContactCategory.SAFETY, "8288081091", "", "Women Help Center and Safety Cell", "9:00 AM – 5:00 PM", Color(0xFFFFD9E2)),
    EmergencyContact("s4", "Office of Fire & Safety Cell", ContactCategory.SAFETY, "01824-444201", "", "Central Fire & Campus Safety Control", "24x7 Emergency", Color(0xFFFFE3C7)),
    EmergencyContact("s5", "Fire & Anti-Ragging Squad — Mr. Sunil Kumar Sharma", ContactCategory.SAFETY, "9878426874", "01824-444661", "Fire & Safety Cell and Anti-Ragging Squad (ARS)", "24x7 Control", Color(0xFFFFE3C7)),
    EmergencyContact("s6", "Fire & Safety — Mr. Anil Kumar Mukhi", ContactCategory.SAFETY, "9855722332", "01824-444272", "Fire and Safety Cell Officer", "24x7 Control", Color(0xFFFFE3C7)),
    EmergencyContact("s7", "Fire & Safety — Brig. Sunil Kumar Saul (Sena Medal)", ContactCategory.SAFETY, "9780005945", "01824-444095", "Chief Security & Fire Safety Cell", "24x7 Control", Color(0xFFFFE3C7)),

    // ── Student Welfare, Relationship & Counseling ─────────────────────────────
    EmergencyContact("w1", "Division of Student Relationship", ContactCategory.WELFARE, "01824-520150", "01824-520099", "Student enquiries, campus records & grievance support", "Block 32, Room 100 · 9 AM – 5 PM (Working Days)", PeersSky),
    EmergencyContact("w2", "Department of Counseling & Happiness", ContactCategory.WELFARE, "01824-444018", "", "Confidential student wellness & mental health counseling", "Block 8, Room 208 · Mon–Sat 9 AM–7 PM, Sun 10 AM–4 PM", PeersMint),
    EmergencyContact("w3", "Division of Student Welfare & Arts/Culture (DSW)", ContactCategory.WELFARE, "01824-444056", "", "Student clubs, cultural societies & welfare wing", "Block 13 · 9:00 AM – 5:00 PM", PeersLavender),

    // ── Residential Facilities (Hostels) ──────────────────────────────────────
    EmergencyContact("r1", "Boys Hostel BH-1 (Blocks A, B, C)", ContactCategory.HOSTELS, "9915020442", "01824-444521", "Landlines: Block A 444521, B 444522, C 444523 · Mobile for Emergency", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r2", "Boys Hostel BH-2 (Blocks A, B)", ContactCategory.HOSTELS, "9888598705", "01824-444524", "Mobile for emergency · Landline during day hours", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r3", "Boys Hostel BH-3 (Blocks A–D)", ContactCategory.HOSTELS, "9876740080", "01824-444526", "Blocks A/B: 01824-444526 · Blocks C/D: 01824-444527", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r4", "Boys Hostel BH-4 (Blocks A–E)", ContactCategory.HOSTELS, "9888598087", "01824-444529", "Mobile for emergency · Landline during day hours", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r5", "Boys Hostel BH-5 (Blocks A, B, C)", ContactCategory.HOSTELS, "9780036434", "01824-444530", "Blocks A/B: 01824-444530 · Block C: 01824-444531", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r6", "Boys Hostel BH-6 (Blocks A, B, C)", ContactCategory.HOSTELS, "9501110445", "01824-444532", "Block A: 01824-444532 · Blocks B/C: 01824-444533", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r7", "Boys Hostel BH-7", ContactCategory.HOSTELS, "7508182814", "01824-444536", "Mobile for emergency · Landline during day hours", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r8", "Boys Hostel BS-11, BS-12 & BS-13", ContactCategory.HOSTELS, "9888598035", "01824-495059", "BS-11: 9888598035 | BS-12: 7986997706 (01824-495065) | BS-13: 9878426873 (01824-495063)", "Landline: 8 AM – 10 PM", PeersMint),
    EmergencyContact("r9", "Campus Apartments (Blocks A–D)", ContactCategory.HOSTELS, "9876015107", "01824-444520", "Blocks C/D: 9876015107 (444520) · Blocks A/B: 9915020440 (444695)", "Landline: 8 AM – 10 PM", PeersSky),
    EmergencyContact("r10", "Girls Hostel GH-1, GH-2, GH-3 & GH-4", ContactCategory.HOSTELS, "9915020443", "01824-444081", "GH-1: 9915020443 | GH-2: 9876644335 | GH-3: 8264042170 | GH-4: 9915020444", "Landline: 8 AM – 10 PM", Color(0xFFFFD9E2)),
    EmergencyContact("r11", "Girls Hostel GH-5, GH-6, GH-7 & GS-12", ContactCategory.HOSTELS, "9876015106", "01824-444303", "GH-5: 9876015106 | GH-6: 9915020439 | GH-7: 9780005963 | GS-12: 7347000907", "Landline: 8 AM – 10 PM", Color(0xFFFFD9E2)),

    // ── National Emergency Helplines ──────────────────────────────────────────
    EmergencyContact("n1", "National Emergency (Police / Fire / Medical)", ContactCategory.NATIONAL, "112", "100", "24x7 All-India emergency response", "Nationwide 24x7", PeersLavender),
    EmergencyContact("n2", "Ambulance Helpline", ContactCategory.NATIONAL, "108", "102", "Emergency medical ambulance dispatch", "Nationwide 24x7", PeersLavender),
    EmergencyContact("n3", "Women Helpline", ContactCategory.NATIONAL, "1091", "181", "24x7 emergency distress support", "Nationwide 24x7", Color(0xFFFFD9E2)),
    EmergencyContact("n4", "UGC National Anti-Ragging Helpline", ContactCategory.NATIONAL, "1800-180-5522", "", "Toll-free campus anti-ragging support", "Nationwide 24x7", PeersSky),
)

@Composable
fun ImportantContactsScreen(
    user: DashboardUser?,
    unreadCount: Int,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ContactCategory.ALL) }

    val visible = remember(query, selectedCategory) {
        officialContacts.filter { c ->
            val catOk = selectedCategory == ContactCategory.ALL || c.category == selectedCategory
            val qOk = query.isBlank() ||
                c.title.contains(query, ignoreCase = true) ||
                c.detail.contains(query, ignoreCase = true) ||
                c.primaryPhone.contains(query, ignoreCase = true) ||
                c.secondaryPhone.contains(query, ignoreCase = true)
            catOk && qOk
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PeersSubScreenHeader(
                title = "Helplines & Contacts",
                subtitle = "Emergency, hostel & campus support numbers",
                user = user,
                unreadCount = unreadCount,
                onBack = onBack,
                onProfileClick = onProfileClick,
                onBellClick = onBellClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search hostel, hospital, block, or number…", fontFamily = Lato, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = PeersPrimaryIndigo,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ContactCategory.entries) { cat ->
                    val chosen = selectedCategory == cat
                    FilterChip(
                        selected = chosen,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                cat.label,
                                fontFamily = GoogleSans,
                                fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PeersPrimaryIndigo,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(visible, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        onCall = { phone -> dialPhone(context, phone) },
                        onCopy = { phone -> copyPhone(context, contact.title, phone) },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: EmergencyContact,
    onCall: (String) -> Unit,
    onCopy: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = contact.accent.copy(alpha = 0.35f),
                ) {
                    Text(
                        text = contact.category.label,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                if (contact.timingOrLocation.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            contact.timingOrLocation,
                            fontFamily = Lato,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Text(
                text = contact.title,
                fontFamily = GoogleSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (contact.detail.isNotBlank()) {
                Text(
                    text = contact.detail,
                    fontFamily = Lato,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilledTonalButton(
                    onClick = { onCall(contact.primaryPhone) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(contact.primaryPhone, fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                if (contact.secondaryPhone.isNotBlank()) {
                    FilledTonalButton(
                        onClick = { onCall(contact.secondaryPhone) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(contact.secondaryPhone, fontFamily = GoogleSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = Color.Transparent,
                ) {
                    IconButton(onClick = { onCopy(contact.primaryPhone) }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = "Copy number",
                            tint = PeersPrimaryIndigo,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun dialPhone(context: Context, phone: String) {
    val clean = phone.filter { it.isDigit() || it == '+' }
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
    runCatching { context.startActivity(intent) }
}

private fun copyPhone(context: Context, label: String, phone: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, phone))
    Toast.makeText(context, "Copied $phone", Toast.LENGTH_SHORT).show()
}
