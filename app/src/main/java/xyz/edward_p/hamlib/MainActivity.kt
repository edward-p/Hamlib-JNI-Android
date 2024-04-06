package xyz.edward_p.hamlib

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.edward_p.hamlib.data.Rig
import xyz.edward_p.hamlib.ui.theme.HamlibTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {

            val allRigs by remember {
                mutableStateOf(HamlibJNI.getInstance().getAllRigs())
            }

            var selectedRig by remember {
                mutableStateOf(allRigs[0])
            }

            val stateText by remember {
                mutableStateOf("Not Connected")
            }

            val manufactureList by remember {
                mutableStateOf(
                    allRigs.map { x -> x.mfgName }.toSortedSet().toList()
                )
            }

            var modelList by remember {
                mutableStateOf(
                    allRigs.filter { x -> x.mfgName.equals(allRigs[0].mfgName) }
                        .map { x -> x.modelName }
                        .sorted()
                        .toList())
            }

            var selectedManufacture by remember {
                mutableStateOf(manufactureList[0])
            }

            var selectedModel by remember {
                mutableStateOf(modelList[0])
            }


            HamlibTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                    ) {

                        DropDown(list = manufactureList,
                            selectedText = selectedManufacture,
                            label = "manufacture",
                            onChange = { selectedText ->
                                run {
                                    modelList =
                                        allRigs.filter { x -> x.mfgName.equals(selectedText) }
                                            .map { x -> x.modelName }
                                            .sorted()
                                            .toList()
                                    selectedManufacture = selectedText
                                    selectedModel = modelList[0]
                                }
                            }
                        )
                        DropDown(list = modelList,
                            selectedText = selectedModel,
                            label = "model",
                            onChange = { selectedText ->
                                run {
                                    selectedModel = selectedText
                                    selectedRig = allRigs.find { rig ->
                                        rig.mfgName.equals(selectedManufacture) && rig.modelName.equals(
                                            selectedModel
                                        )
                                    }!!
                                }
                            })


                        Button(onClick = {
                            HamlibJNI.getInstance().rigInit(selectedRig.rigModel)
                        }) {
                            Text("rig_init")
                        }
                        Button(onClick = {
                            HamlibJNI.getInstance().rigOpen(Pty.getInstance().devname)
                        }) {
                            Text("rig_open")
                        }

                        Button(onClick = {
                            HamlibJNI.getInstance().rigSetFreq(HamlibJNI.RIG_VFO_A, 438_500_000.0)
                        }) {
                            Text("Set VFO_A: 438.500")
                        }

                        Button(onClick = {
                            HamlibJNI.getInstance().rigSetFreq(HamlibJNI.RIG_VFO_B, 144_500_000.0)
                        }) {
                            Text("Set VFO_B: 144.500")
                        }

                        Button(onClick = { HamlibJNI.getInstance().rigClose() }) {
                            Text("rig_close")
                        }

                        ButtonBar(onClick = {
                            val intent = Intent(this@MainActivity, BluetoothActivity::class.java)
                            startActivity(intent)
                        })
                    }
                }
            }
        }
    }
}


@Composable
fun ButtonBar(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(bottom = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,

        ) {
        FloatingActionButton(onClick = onClick) {
            Icon(Icons.Default.Add, "Connect")
        }
    }
}

@Composable
fun CenteredText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(
    list: List<String>,
    selectedText: String,
    label: String,
    onChange: (selectedText: String) -> Unit = {}
) {

    var isExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }) {
            TextField(
                modifier = Modifier.menuAnchor(),
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) })


            ExposedDropdownMenu(
                expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                list.forEachIndexed { index, text ->
                    DropdownMenuItem(
                        text = { Text(text = text) },
                        onClick = {
                            isExpanded = false
                            onChange(text)
                        })
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val text = remember {
        mutableStateOf("Not Connected")
    }

    HamlibTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                DropDown(list = listOf(""),
                    label = "manufacture",
                    selectedText = "",
                    onChange = {}
                )
                DropDown(list = listOf("IC-705", "IC-706", "IC-7300"),
                    selectedText = "",
                    label = "model",
                    onChange = {})
                Button(onClick = { /*TODO*/ }) {
                    Text("rig_init")
                }

                ButtonBar({})
            }
        }
    }
}
