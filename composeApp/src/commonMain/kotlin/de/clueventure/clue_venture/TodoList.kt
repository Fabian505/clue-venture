package de.clueventure.clue_venture

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private val supabase = createSupabaseClient(
    supabaseUrl = "https://havgcmueimjjqjtvzjpr.supabase.co",
    supabaseKey = "sb_publishable_dlXFI6T4et5Xqkn-gsyycw_LvDOqFJ_"
) {
    install(Postgrest)
}

@Composable
fun TodoList() {
    var items by remember { mutableStateOf<List<TodoItem>>(listOf()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            items = supabase.from("todos")
                .select().decodeList<TodoItem>()
        }
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items,
            key = { item -> item.id },
        ) { item ->
            Text(
                text = item.name,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
