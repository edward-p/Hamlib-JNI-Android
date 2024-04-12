package xyz.edward_p.hamlib

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.edward_p.hamlib.data.Rig
import xyz.edward_p.hamlib.service.HamlibSPPService
import xyz.edward_p.hamlib.ui.theme.HamlibTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {

            val allRigs by remember {
                mutableStateOf(HamlibJNI.instance.getAllRigs())
            }

            val manufactureList by remember {
                mutableStateOf(
                    allRigs.map { x -> x.mfgName }.toSortedSet().toList()
                )
            }

            var modelList by remember {
                mutableStateOf(
                    allRigs.filter { x -> x.mfgName == allRigs[0].mfgName }
                        .map { x -> x.modelName }
                        .sorted()
                        .toList())
            }

            var selectedManufacturer by remember { mutableIntStateOf(-1) }
            var selectedModel by remember { mutableIntStateOf(-1) }
            var selectedRig: Rig? by remember { mutableStateOf(null) }

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

                        LargeDropdownMenu(
                            label = "manufacture",
                            items = manufactureList,
                            selectedIndex = selectedManufacturer,
                            onItemSelected = { index, text ->
                                run {
                                    selectedManufacturer = index
                                    modelList = allRigs.filter { x -> x.mfgName == text }
                                        .map { x -> x.modelName }
                                        .sorted()
                                        .toList()
                                }
                            },
                        )

                        LargeDropdownMenu(
                            label = "model",
                            items = modelList,
                            selectedIndex = selectedModel,
                            onItemSelected = { index, _ ->
                                run {
                                    selectedModel = index
                                    selectedRig =
                                        allRigs.find { x ->
                                            x.mfgName == manufactureList[selectedManufacturer]
                                                    && x.modelName == modelList[index]

                                        }
                                }
                            },
                        )

                        Button(onClick = {
                            HamlibJNI.instance.rigInit(selectedRig!!.rigModel)
                        }) {
                            Text("rig_init")
                        }
                        Button(onClick = {
                            HamlibJNI.instance.rigOpen()
                        }) {
                            Text("rig_open")
                        }

                        Button(onClick = {
                            HamlibJNI.instance.rigSetFreq(HamlibJNI.RIG_VFO_A, 438_500_000.0)
                        }) {
                            Text("Set VFO_A: 438.500")
                        }

                        Button(onClick = {
                            HamlibJNI.instance.rigSetFreq(HamlibJNI.RIG_VFO_B, 144_500_000.0)
                        }) {
                            Text("Set VFO_B: 144.500")
                        }

                        Button(onClick = {
                            HamlibJNI.instance.rigCleanUp()
//                            stopService(Intent(this@MainActivity,HamlibSPPService::class.java))
                        }) {
                            Text("rig_cleanup")
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

@Preview(showBackground = true)
@Composable
fun Preview() {

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


                var selectedManufacturer by remember { mutableIntStateOf(-1) }
                LargeDropdownMenu(
                    label = "manufacture",
                    items = listOf(""),
                    selectedIndex = selectedManufacturer,
                    onItemSelected = { index, _ -> selectedManufacturer = index },
                )

                var selectedModel by remember { mutableIntStateOf(-1) }
                LargeDropdownMenu(
                    label = "model",
                    items = listOf("IC-705", "IC-706", "IC-7300"),
                    selectedIndex = selectedModel,
                    onItemSelected = { index, _ -> selectedModel = index },
                )

                Button(onClick = { /*TODO*/ }) {
                    Text("rig_init")
                }

                ButtonBar({})
            }
        }
    }
}
