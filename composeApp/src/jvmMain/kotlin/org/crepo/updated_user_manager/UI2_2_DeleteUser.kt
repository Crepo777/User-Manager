package org.crepo.updated_user_manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UI2_2(){
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.LightGray)) {
        Text(
            text = "Удаление пользователя",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "Введите имя пользователя, которого хотите удалить:",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 350.dp)
        )
        Text(
            text = "ПОЛЕ ВВОДА",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 275.dp)
        )
        Text(
            text = "ВНИМАНИЕ!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 200.dp)
        )
        Text(
            text = "Это необратимое действие и его нельзя будет отменить!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp)
        )
        Text(
            text = "Будет удалена папка данного пользователя и его файлы.",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 50.dp)
        )
        Button(
            modifier = Modifier
                .padding(top = 100.dp)
                .align(Alignment.Center),
            onClick = {}
        ) {
            Text(text = "Удалить", fontSize = 30.sp)
        }
        Button(
            modifier = Modifier
                .padding(top = 230.dp)
                .align(Alignment.Center),
            onClick = {}
        ) {
            Text(text = "Назад", fontSize = 30.sp)
        }
    }

}
@Composable
@Preview(widthDp = 1920, heightDp = 1080)
private fun Preview() {
    MaterialTheme {
        UI2_2()
    }
}