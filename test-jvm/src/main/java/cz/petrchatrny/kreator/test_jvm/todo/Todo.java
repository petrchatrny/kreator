package cz.petrchatrny.kreator.test_jvm.todo;

import java.time.LocalDateTime;
import java.util.Map;

import cz.petrchatrny.kreator.annotations.Dto;
import cz.petrchatrny.kreator.annotations.DtoFields;
import cz.petrchatrny.kreator.annotations.GenerateGetters;
import cz.petrchatrny.kreator.annotations.Kreator;

//@Kreator(
//        dtos = {
//                @Dto(name = "TodoCreateDto", omit = {TodoFields.id, TodoFields.createdAt})
//        }
//)
@DtoFields
@GenerateGetters
public class Todo {
    public long id;
    public String title;
    public Boolean isCompleted;
    public Map<String, String> properties;
    public LocalDateTime createdAt;

    public Todo(long id, String title, Boolean isCompleted, Map<String, String> properties, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.properties = properties;
        this.createdAt = createdAt;
    }
}
