package com.microservicios.tasks.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.microservicios.tasks.dto.TaskDto;
import com.microservicios.tasks.dto.TaskListDto;

@Service
public class TasksService {

    private static final Logger logger = Logger.getLogger(TasksService.class.getName());

    public List<TaskListDto> listTasks(String accessTokenString) {
        List<TaskListDto> taskListsResponse = new ArrayList<>();
        try {
            logger.info("Iniciando busqueda de eventos con access token");

            // Armar las credenciales con el access token
            AccessToken accessToken = new AccessToken(accessTokenString, null);
            GoogleCredentials credentials = GoogleCredentials.create(accessToken);

            // Armar el servicio de task
            com.google.api.services.tasks.Tasks service =
                new com.google.api.services.tasks.Tasks.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                .setApplicationName("MobyApp Tasks Microservice")
                .build();

            // Traer la lista de las task
            com.google.api.services.tasks.model.TaskLists taskLists = service.tasklists().list().execute();

            if (taskLists.getItems() != null && !taskLists.getItems().isEmpty()) {
                for (com.google.api.services.tasks.model.TaskList taskListItem : taskLists.getItems()) {
                    String listId = taskListItem.getId();
                    String listTitle = taskListItem.getTitle() != null ? taskListItem.getTitle() : "Sin title";

                    // crear tasklistdto
                    TaskListDto taskListDto = new TaskListDto(listId, listTitle, "tasklist");
                    List<TaskDto> tasksInList = new ArrayList<>();

                    // traer los eventos desde la lista
                    com.google.api.services.tasks.model.Tasks tasks = service.tasks()
                        .list(listId)
                        .setMaxResults(50) // Aumentar limite
                        .execute();

                    if (tasks.getItems() != null) {
                        for (com.google.api.services.tasks.model.Task task : tasks.getItems()) {
                            String taskId = task.getId() != null ? task.getId() : "";
                            String title = task.getTitle() != null ? task.getTitle() : "Sin titulo";
                            String status = task.getStatus() != null ? task.getStatus() : "needsAction";
                            String dueDate = task.getDue() != null ? task.getDue() : "Sin vencimiento";
                            String notes = task.getNotes() != null ? task.getNotes() : "";

                            tasksInList.add(new TaskDto(taskId, title, status, dueDate, notes, "task"));
                        }
                    }

                    taskListDto.setTasks(tasksInList);
                    taskListsResponse.add(taskListDto);
                }
                logger.info("Se encontraron " + taskListsResponse.size() + " task list");
            } else {
                logger.info("No se encontrar lista de eventos");
            }

        } catch (Exception e) {
            logger.severe("Error escuchando eventos: " + e.getMessage());
            e.printStackTrace();
        }
        return taskListsResponse;
    }

    public boolean markTaskComplete(String taskId, String accessTokenString) {
        try {
            logger.info("Marcando tarea como completada: " + taskId);

            com.google.api.services.tasks.Tasks service = createTasksService(accessTokenString);
            TaskSearchResult searchResult = findTaskById(service, taskId);

            if (!searchResult.found()) {
                logger.warning("No se encontró la tarea con ID: " + taskId);
                return false;
            }

            return updateTaskStatus(service, searchResult, "completed");

        } catch (Exception e) {
            logger.severe("Error marcando tarea como completada: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private com.google.api.services.tasks.Tasks createTasksService(String accessTokenString) throws IOException {
        AccessToken accessToken = new AccessToken(accessTokenString, null);
        GoogleCredentials credentials = GoogleCredentials.create(accessToken);

        return new com.google.api.services.tasks.Tasks.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
            .setApplicationName("MobyApp Tasks Microservice")
            .build();
    }

    private TaskSearchResult findTaskById(com.google.api.services.tasks.Tasks service, String taskId) throws IOException {
        com.google.api.services.tasks.model.TaskLists taskLists = service.tasklists().list().execute();

        if (taskLists.getItems() == null) {
            return TaskSearchResult.notFound();
        }

        for (com.google.api.services.tasks.model.TaskList taskList : taskLists.getItems()) {
            try {
                com.google.api.services.tasks.model.Tasks tasks = service.tasks()
                    .list(taskList.getId())
                    .execute();

                if (tasks.getItems() != null) {
                    for (com.google.api.services.tasks.model.Task task : tasks.getItems()) {
                        if (taskId.equals(task.getId())) {
                            return TaskSearchResult.found(taskList.getId(), task);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warning("Error buscando en lista " + taskList.getId() + ": " + e.getMessage());
            }
        }

        return TaskSearchResult.notFound();
    }

    private boolean updateTaskStatus(com.google.api.services.tasks.Tasks service,
                                   TaskSearchResult searchResult, String status) throws IOException {
        searchResult.getTask().setStatus(status);
        service.tasks().update(searchResult.getTaskListId(),
                              searchResult.getTask().getId(),
                              searchResult.getTask()).execute();

        logger.info("Tarea marcada como completada exitosamente: " + searchResult.getTask().getId());
        return true;
    }

    // Clase auxiliar para el resultado de búsqueda
    private static class TaskSearchResult {
        private final String taskListId;
        private final com.google.api.services.tasks.model.Task task;
        private final boolean found;

        private TaskSearchResult(String taskListId, com.google.api.services.tasks.model.Task task, boolean found) {
            this.taskListId = taskListId;
            this.task = task;
            this.found = found;
        }

        static TaskSearchResult found(String taskListId, com.google.api.services.tasks.model.Task task) {
            return new TaskSearchResult(taskListId, task, true);
        }

        static TaskSearchResult notFound() {
            return new TaskSearchResult(null, null, false);
        }

        boolean found() { return found; }
        String getTaskListId() { return taskListId; }
        com.google.api.services.tasks.model.Task getTask() { return task; }
    }
}