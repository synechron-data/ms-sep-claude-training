import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Task, TaskService } from './task.service';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="task-page">
      <h2>My Tasks</h2>

      <div class="new-task">
        <input [(ngModel)]="newTitle" placeholder="Task title" />
        <input [(ngModel)]="newDescription" placeholder="Description" />
        <button (click)="addTask()">Add</button>
      </div>

      <ul>
        <li *ngFor="let task of tasks" [class.completed]="task.completed">
          <span>{{ task.title }}</span>
          <div class="actions">
            <button *ngIf="!task.completed" (click)="complete(task)">Complete</button>
            <button (click)="remove(task)">Delete</button>
          </div>
        </li>
      </ul>
    </div>
  `,
  styles: [
    `
      .task-page { max-width: 480px; margin: 2rem auto; }
      .new-task { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
      ul { list-style: none; padding: 0; }
      li { display: flex; justify-content: space-between; align-items: center; padding: 0.5rem; border-bottom: 1px solid #e5e7eb; }
      li.completed span { text-decoration: line-through; color: #9ca3af; }
      .actions button { margin-left: 0.25rem; }
    `,
  ],
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  newTitle = '';
  newDescription = '';

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.taskService.list().subscribe((tasks) => (this.tasks = tasks));
  }

  addTask(): void {
    if (!this.newTitle.trim()) {
      return;
    }
    this.taskService.create(this.newTitle, this.newDescription).subscribe(() => {
      this.newTitle = '';
      this.newDescription = '';
      this.refresh();
    });
  }

  complete(task: Task): void {
    this.taskService.complete(task.id).subscribe(() => this.refresh());
  }

  remove(task: Task): void {
    this.taskService.delete(task.id).subscribe(() => this.refresh());
  }
}
