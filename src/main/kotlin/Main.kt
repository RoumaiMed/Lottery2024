import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.exp

data class Config(
    val members: ArrayList<String>,
    val configs: ArrayList<ConfigItem>,
)

data class ConfigItem(
    val title: String,
    val row: Int,          // 展示的 row 数
    val col: Int,          // 展示的 col 数
    val targetNumber: Int, // 最终获奖人数
)


val _RED = Color(237, 106, 95)

fun rearrange(members: List<String>): ArrayList<String> {
    val result = ArrayList<String>(members.size)
    val temp = ArrayList<String>(members.size)
    temp.addAll(members)
    for (i in members.indices) {
        val index = (Math.random() * temp.size).toInt()
//        // +去重
//        if (result.contains(temp[index])) {
//            continue
//        }
        result.add(temp[index])
        temp.removeAt(index)
    }
    return result
}

fun MutableList<String>.removeDuplicates(): MutableList<String> {
    return this.toSet().toMutableList()
}

@Composable
fun RandomButton(
    id: String,
    members: MutableList<String>,
    triggerCount: Int,
    size: DpSize,
    longDelay: Boolean = false,
    onReset: () -> Unit,
    onSelected: (String) -> Unit
) {
    var working by remember { mutableStateOf(false) }
    var rearrangedMembers by remember { mutableStateOf(rearrange(members).toMutableList()) }
    var memberIndex by remember { mutableStateOf(0) }
    var member by remember { mutableStateOf(rearrangedMembers[memberIndex]) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        rearrangedMembers.clear()
        rearrangedMembers = rearrange(members).toMutableList()
        memberIndex = 0
        member = rearrangedMembers[memberIndex]
    }

    val triggerOne = suspend {
        withContext(Dispatchers.IO) {
            val memberSize = rearrangedMembers.size
            // 随机播放一秒
            var index = 0
            val total = if (longDelay) 50 else 20
            while (working && index++ < total) {
                delay(30)
                member = rearrangedMembers[index % rearrangedMembers.size]
            }
            // target id:
            val target = (memberSize / 2 + 1) % memberSize
            memberIndex = (target - 6) % memberSize
            var count = 0
            while (working && count++ < 6) {
                index = memberIndex++ % memberSize
                member = rearrangedMembers[index]
                delay((100 * exp(index * 3.6f / memberSize)).toLong())
                if (count == 5) { // 多等一段时间展示最终结果
                    delay(200)
                }
            }
            // 闪烁一下
            val name = member
            count = 0
            val total2 = if (longDelay) 2 else 1
            while (count++ < total2) {
                member = ""
                delay(200)
                member = name
                delay(400)
            }
            member = name
            // take it.
            onSelected(member)
            // clear and rearrange.
            rearrangedMembers.remove(member) // 去掉已中奖的人
            rearrangedMembers = rearrange(rearrangedMembers).toMutableList()
        }
    }

    Row {
        if (working) {
            OutlinedButton(
                modifier = Modifier.size(size.width * 2, size.height).padding(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = _RED,
                    contentColor = Color.White,
                ),
                onClick = {
                    // do nothing.
                }) {
                Text(
                    member,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            if (members.size - triggerCount >= rearrangedMembers.size) {
                OutlinedButton(
                    modifier = Modifier.size(size.width * 2, size.height).padding(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = _RED,
                        contentColor = Color.White,
                    ),
                    onClick = {
                        onReset()
                        rearrangedMembers = rearrange(members).toMutableList()
                    }) {
                    Text(
                        "重置",
                        fontSize = 86.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        modifier = Modifier.size(96.dp),
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "next"
                    )
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.size(size.width * 2, size.height).padding(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = _RED,
                        contentColor = Color.White,
                    ),
                    onClick = {
                        if (working) {
                            return@OutlinedButton
                        }
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                working = true
                                var i = 0
                                while (i++ < triggerCount) {
                                    triggerOne()
                                    delay(1000)
                                }
                                working = false
                            }
                        }
                    }) {
                    Text(
                        "开始",
                        fontSize = 86.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        modifier = Modifier.size(96.dp),
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "next"
                    )
                }
            }
        }
    }
}

@Composable
fun CardN(
    title: String,
    members: MutableList<String>,
    triggerCount: Int,
    row: Int,
    col: Int,
    itemSize: DpSize,
    onNext: (List<String>) -> Unit
) {

    val selectedMembers = remember { mutableStateListOf<String>() }

    LaunchedEffect(title) {
        selectedMembers.clear()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier.height(itemSize.height),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(title, fontSize = 72.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (triggerCount == 1) {
                Box(
                    modifier = Modifier.size(itemSize.width * 2, itemSize.height), contentAlignment = Alignment.Center
                ) {
                    if (selectedMembers.isNotEmpty()) {
                        Text(
                            text = selectedMembers[0],
                            fontSize = 156.sp,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Visible,
                            maxLines = 1,
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(if (row < 3) 16.dp else 2.dp)
                ) {
                    for (j in 0 until row) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (k in 0 until col) {
                                Box(modifier = Modifier.size(itemSize), contentAlignment = Alignment.Center) {
                                    val index = j * col + k
                                    if (index < selectedMembers.size) {
                                        Text(
                                            text = selectedMembers[index],
                                            fontSize = if (row < 3) 86.sp else 64.sp,
                                            fontWeight = FontWeight.Bold,
                                            overflow = TextOverflow.Visible,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(itemSize.height * 3),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RandomButton(
                id = "${title}-id",
                members = members,
                triggerCount = triggerCount,
                size = itemSize,
                onReset = {
                    selectedMembers.clear()
                }) {
                println("Selected: $it")
                if (selectedMembers.contains(it)) return@RandomButton
                selectedMembers.add(it)
            }
            if (triggerCount >= 1 && selectedMembers.size >= triggerCount) {
                OutlinedButton(
                    modifier = Modifier.size(itemSize.width * 2, itemSize.height).padding(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = _RED,
                        contentColor = Color.White,
                    ),
                    onClick = {
                        selectedMembers.clear()
                        onNext(selectedMembers)
                    }) {
                    Text(
                        "下一轮",
                        fontSize = 86.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        modifier = Modifier.size(96.dp),
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "next"
                    )
                }
            }
        }
    }
}

fun ArrayList<String>.removeOnce(items: Collection<String>) {
    val items = items.toSet().toMutableList()

    for (i in 0 until size) {
        if (this[i] in items) {
            removeAt(i)
            items.remove(this[i])
            continue
        }
    }
}

@Composable
fun RoundN(size: DpSize, config: Config, onNext: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val members by remember { mutableStateOf(config.members) }

    if (step < config.configs.size) {
        val card = config.configs[step]
        CardN(
            card.title,
            members = members.removeDuplicates(),
            triggerCount = card.targetNumber,
            row = card.row,
            col = card.col,
            itemSize = size / 5
        ) {
            if (step < config.configs.size) {
                // display next title
                println(config.configs[step].title)
            }
            step++
            members.removeOnce(it)
        }
    } else {
        // go to next card
        onNext()
    }
}

@Composable
@Preview
fun App(size: DpSize) {
    var configIdx by remember { mutableStateOf(0) }

    MaterialTheme {
        if (configIdx == 0) {
            RoundN(
                size = size,
                config = CONFIGS[0]
            ) {
                configIdx++
            }
        } else if (configIdx == 1) {
            RoundN(
                size = size,
                config = CONFIGS[1]
            ) {
                configIdx++
            }
        } else {
            println("End")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("抽奖结束！")
            }
        }
    }
}

fun main() = application {
    val state = rememberWindowState(placement = WindowPlacement.Maximized)

    Window(
        title = "\uD83C\uDFC5\uFE0F柔脉爆金币神器｜十连抽必中大奖｜抽了还能重置抽｜抽到手抽｜抽到不认识抽字\uD83C\uDFC5\uFE0F",
        onCloseRequest = ::exitApplication,
        state = state
    ) {
        App(state.size)
    }
}
