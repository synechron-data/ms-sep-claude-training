import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Task {
  id: number;
  title: string;
  description: string;
  completed: boolean;
  ownerId: number;
  createdAt: string;
  dueDate: string | null;
  overdue: boolean;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly baseUrl = 'http://localhost:8080/api/tasks';

  constructor(private http: HttpClient) {}

  list(): Observable<Task[]> {
    return this.http.get<Task[]>(this.baseUrl);
  }

  create(title: string, description: string, dueDate: string | null): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, { title, description, dueDate });
  }

  update(id: number, title: string, description: string, dueDate: string | null): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, { title, description, dueDate });
  }

  complete(id: number): Observable<Task> {
    return this.http.patch<Task>(`${this.baseUrl}/${id}/complete`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
