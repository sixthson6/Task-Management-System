package com.novatech.controller;

import com.novatech.dao.TaskDAO;
import com.novatech.model.Status;
import com.novatech.model.Task;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/tasks")
public class TaskController extends HttpServlet {

    private TaskDAO taskDAO;

    @Override
    public void init() throws ServletException {
        taskDAO = new TaskDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            String sort = req.getParameter("sort");
            String selectedStatus = req.getParameter("status");
            if (sort == null) {
                sort = "asc";
            }
            if (selectedStatus == null || selectedStatus.isEmpty()) {
                selectedStatus = "ALL";
            }

            if ("edit".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                Task task = taskDAO.getTaskById(id);
                req.setAttribute("task", task);
                req.getRequestDispatcher("/edit-task.jsp").forward(req, resp);
                return;
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                taskDAO.deleteTask(id);
                resp.sendRedirect("tasks");
                return;
            }

            int page = 1;
            int pageSize = 10;
            if (req.getParameter("page") != null) {
                page = Integer.parseInt(req.getParameter("page"));
            }

            int totalTasks = taskDAO.getTaskCount();
            int totalPages = (int) Math.ceil((double) totalTasks / pageSize);
            int offset = (page - 1) * pageSize;

            List<Task> tasks = taskDAO.getTasksWithPaginationSortingAndStatus(pageSize, offset, sort, selectedStatus);

            req.setAttribute("tasks", tasks);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("sort", sort);
            req.setAttribute("selectedStatus", selectedStatus);
            req.getRequestDispatcher("/task-list.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");

            if ("update".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                LocalDate dueDate = LocalDate.parse(req.getParameter("dueDate"));
                Status status = Status.valueOf(req.getParameter("status"));

                Task existingTask = taskDAO.getTaskById(id);
                if (existingTask == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
                    return;
                }

                LocalDateTime createdAt = existingTask.getCreatedAt();
                LocalDateTime updatedAt = LocalDateTime.now();

                Task task = new Task(id, title, description, dueDate, status, createdAt, updatedAt);
                taskDAO.updateTask(task);
                resp.sendRedirect("tasks");
            } else {
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                LocalDate dueDate = LocalDate.parse(req.getParameter("dueDate"));
                Status status = Status.valueOf(req.getParameter("status"));

                Task task = new Task(title, description, dueDate, status);
                taskDAO.addTask(task);
                resp.sendRedirect("tasks");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}