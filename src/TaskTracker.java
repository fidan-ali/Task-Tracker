import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskTracker {
    private static final String FILE_NAME = "tasks.json";
    private static final Scanner scanner = new Scanner(System.in);

    static class Task {
        int id;
        String description;
        String status;
        String createdAt;
        String updatedAt;

        public Task(int id, String description, String status) {
            this.id = id;
            this.description = description;
            this.status = status;
            this.createdAt = now();
            this.updatedAt = now();
        }

        static String now() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Override
        public String toString() {
            return String.format("[%d] %s | %s | Created: %s | Updated: %s",
                    id, description, status, createdAt, updatedAt);
        }

        public String toJSON() {
            return String.format(
                    "{\"id\":%d,\"description\":\"%s\",\"status\":\"%s\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
                    id, description.replace("\"", "\\\""), status, createdAt, updatedAt
            );
        }

        public static Task fromJSON(String json) {
            Map<String, String> map = new HashMap<>();
            json = json.replaceAll("[{}\"]", "");
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    map.put(kv[0], kv[1]);
                }
            }
            Task t = new Task(
                    Integer.parseInt(map.get("id")),
                    map.get("description"),
                    map.get("status")
            );
            t.createdAt = map.get("createdAt");
            t.updatedAt = map.get("updatedAt");
            return t;
        }
    }

    private static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return tasks;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    tasks.add(Task.fromJSON(line.trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return tasks;
    }

    private static void saveTasks(List<Task> tasks) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Task t : tasks) {
                pw.println(t.toJSON());
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private static int getNextId(List<Task> tasks) {
        return tasks.stream().mapToInt(t -> t.id).max().orElse(0) + 1;
    }

    private static void addTask(String description) {
        List<Task> tasks = loadTasks();
        Task newTask = new Task(getNextId(tasks), description, "todo");
        tasks.add(newTask);
        saveTasks(tasks);
        System.out.println("Task added successfully (ID: " + newTask.id + ")");
    }

    private static void updateTask(int id, String newDescription) {
        List<Task> tasks = loadTasks();
        for (Task t : tasks) {
            if (t.id == id) {
                t.description = newDescription;
                t.updatedAt = Task.now();
                saveTasks(tasks);
                System.out.println("Task updated successfully.");
                return;
            }
        }
        System.out.println("Task not found!");
    }

    private static void deleteTask(int id) {
        List<Task> tasks = loadTasks();
        boolean removed = tasks.removeIf(t -> t.id == id);
        if (removed) {
            saveTasks(tasks);
            System.out.println("Task deleted successfully.");
        } else {
            System.out.println("Task not found!");
        }
    }

    private static void markStatus(int id, String status) {
        List<Task> tasks = loadTasks();
        for (Task t : tasks) {
            if (t.id == id) {
                t.status = status;
                t.updatedAt = Task.now();
                saveTasks(tasks);
                System.out.println("Task marked as " + status);
                return;
            }
        }
        System.out.println("Task not found!");
    }

    private static void listTasks(String filter) {
        List<Task> tasks = loadTasks();
        if (tasks.isEmpty()) {
            System.out.println("No tasks found!");
            return;
        }
        for (Task t : tasks) {
            if (filter == null || t.status.equals(filter)) {
                System.out.println(t);
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n=== TASK TRACKER MENU ===");
        System.out.println("1. Add task");
        System.out.println("2. Update task");
        System.out.println("3. Delete task");
        System.out.println("4. Mark task in-progress");
        System.out.println("5. Mark task done");
        System.out.println("6. List all tasks");
        System.out.println("7. List tasks by status");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    public static void main(String[] args) {
        while (true) {
            showMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter task description: ");
                    addTask(scanner.nextLine());
                    break;
                case "2":
                    System.out.print("Enter task ID to update: ");
                    int updateId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter new description: ");
                    updateTask(updateId, scanner.nextLine());
                    break;
                case "3":
                    System.out.print("Enter task ID to delete: ");
                    deleteTask(Integer.parseInt(scanner.nextLine()));
                    break;
                case "4":
                    System.out.print("Enter task ID to mark in-progress: ");
                    markStatus(Integer.parseInt(scanner.nextLine()), "in-progress");
                    break;
                case "5":
                    System.out.print("Enter task ID to mark done: ");
                    markStatus(Integer.parseInt(scanner.nextLine()), "done");
                    break;
                case "6":
                    listTasks(null);
                    break;
                case "7":
                    System.out.print("Enter status to filter (todo/in-progress/done): ");
                    listTasks(scanner.nextLine());
                    break;
                case "0":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }
}
