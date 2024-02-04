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

val _RED = Color(237, 106, 95)

enum class Step {
    Lucky, Forth, Third, Second, First,
}

fun rearrange(members: List<String>): ArrayList<String> {
    val result = ArrayList<String>(members.size)
    val temp = ArrayList<String>(members.size)
    temp.addAll(members)
    for (i in members.indices) {
        val index = (Math.random() * temp.size).toInt()
        result.add(temp[index])
        temp.removeAt(index)
    }
    return result
}

@Composable
fun RandomButton(
    members: MutableList<String>,
    triggerCount: Int,
    size: DpSize,
    longDelay: Boolean = true,
    onReset: () -> Unit,
    onSelected: (String) -> Unit
) {
    var working by remember { mutableStateOf(false) }
    var rearrangedMembers by remember { mutableStateOf(rearrange(members).toMutableList()) }
    var memberIndex by remember { mutableStateOf(0) }
    var member by remember { mutableStateOf(rearrangedMembers[memberIndex]) }

    val scope = rememberCoroutineScope()

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
            OutlinedButton(modifier = Modifier.size(size.width * 2, size.height).padding(24.dp),
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

                OutlinedButton(modifier = Modifier.size(size.width * 2, size.height).padding(24.dp),
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
                OutlinedButton(modifier = Modifier.size(size.width * 2, size.height).padding(24.dp),
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
    title: String, members: MutableList<String>, triggerCount: Int, itemSize: DpSize, onNext: (List<String>) -> Unit
) {

    val selectedMembers = remember { mutableStateListOf<String>() }

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
                for (j in 0 until triggerCount) {
                    Box(modifier = Modifier.size(itemSize), contentAlignment = Alignment.Center) {
                        val index = j
                        if (index < selectedMembers.size) {
                            Text(
                                text = selectedMembers[index],
                                fontSize = 86.sp,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Visible,
                                maxLines = 1,
                            )
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
            RandomButton(members = members, triggerCount = triggerCount, size = itemSize, onReset = {
                selectedMembers.clear()
            }) {
                println("Selected: $it")
                if (selectedMembers.contains(it)) return@RandomButton
                selectedMembers.add(it)
            }
            if (triggerCount > 1 && selectedMembers.size >= triggerCount) {
                OutlinedButton(modifier = Modifier.size(itemSize.width * 2, itemSize.height).padding(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = _RED,
                        contentColor = Color.White,
                    ),
                    onClick = { onNext(selectedMembers) }) {
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

@Composable
fun Card20(title: String, members: MutableList<String>, itemSize: DpSize, onNext: (List<String>) -> Unit) {

    val selectedMembers = remember { mutableStateListOf<String>() }
    val size = DpSize(itemSize.width, itemSize.height - 12.dp)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(), // .height(itemSize.height),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(title, fontSize = 72.sp, fontWeight = FontWeight.Bold)
        }
        for (i in 0 until 2) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 5) {
                    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
                        val index = i * 5 + j
                        if (index < selectedMembers.size) {
                            Text(
                                text = selectedMembers[index],
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            RandomButton(members = members, triggerCount = 20, size = size, longDelay = false, onReset = {
                selectedMembers.clear()
            }) {
                println("Selected: $it")
                if (selectedMembers.contains(it)) return@RandomButton
                selectedMembers.add(it)
            }
            if (selectedMembers.size >= 20) {
                OutlinedButton(modifier = Modifier.size(size.width * 2, size.height).padding(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = _RED,
                        contentColor = Color.White,
                    ),
                    onClick = { onNext(selectedMembers) }) {
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
        for (i in 2 until 4) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 5) {
                    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
                        val index = i * 5 + j
                        if (index < selectedMembers.size) {
                            Text(
                                text = selectedMembers[index],
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun App(size: DpSize) {
    val members by remember { mutableStateOf(MEMBERS.toMutableList()) }
    var step by remember { mutableStateOf(Step.Lucky) }

    MaterialTheme {
        when (step) {
            Step.Lucky -> Card20(
                "\uD83C\uDFC5\uFE0F纪念奖\uD83C\uDFC5\uFE0F", members = members.toMutableList(), size / 5
            ) {
                step = Step.Forth
                members.removeAll(it)
                println("Next: 四等奖")
            }

            Step.Forth -> CardN(
                "\uD83C\uDFC5\uFE0F四等奖\uD83C\uDFC5\uFE0F",
                members = members.toMutableList(),
                triggerCount = 4,
                size / 5
            ) {
                step = Step.Third
                members.removeAll(it)
                println("Next: 三等奖")
            }

            Step.Third -> CardN(
                "\uD83C\uDFC5\uFE0F三等奖\uD83C\uDFC5\uFE0F",
                members = members.toMutableList(),
                triggerCount = 3,
                size / 5
            ) {
                step = Step.Second
                members.removeAll(it)
                println("Next: 二等奖")
            }

            Step.Second -> CardN(
                "\uD83C\uDFC5\uFE0F二等奖\uD83C\uDFC5\uFE0F",
                members = members.toMutableList(),
                triggerCount = 2,
                size / 5
            ) {
                step = Step.First
                members.removeAll(it)
                println("Next: 一等奖")
            }

            Step.First -> CardN(
                "\uD83C\uDFC5\uFE0F一等奖\uD83C\uDFC5\uFE0F",
                members = members.toMutableList(),
                triggerCount = 1,
                size / 5
            ) {
                members.removeAll(it)
                println("抽奖结束")
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
