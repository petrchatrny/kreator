package cz.petrchatrny.kreator.test_jvm.todo

import cz.petrchatrny.kreator.test_jvm.TodoGetters.getProperties
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TodoService {

    @PostConstruct
    fun init() {
        val todos = listOf(
            Todo(
                1,
                "Fill dishwasher",
                false,
                emptyMap(),
                LocalDateTime.now()
            ),
            Todo(
                2,
                "Take out trash",
                true,
                mapOf("priority" to "low"),
                LocalDateTime.now().minusHours(2)
            ),
            Todo(
                3,
                "Write project report",
                false,
                mapOf("due" to "2025-07-25", "category" to "work"),
                LocalDateTime.now().minusDays(1)
            ),
            Todo(
                4,
                "Buy groceries",
                false,
                mapOf("store" to "Lidl", "priority" to "medium"),
                LocalDateTime.now().minusMinutes(30)
            )
        )
        println("Total todos: ${todos.size}")
        println("Properties of Todo with id 3: ${todos[2].getProperties()}")
    }
}