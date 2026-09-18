import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
}

export interface TaskListOptions {
  priority?: Array<'LOW' | 'MEDIUM' | 'HIGH'>;
  status?: 'complete' | 'incomplete';
  sort?: 'priority';
  page?: number;
  size?: number;
}

export interface TaskPage {
  items: Task[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly baseUrl = 'http://localhost:8080/api/tasks';

  constructor(private http: HttpClient) {}

  // Returns a plain array when neither page nor size is sent (today's unchanged
  // shape) or a TaskPage wrapper when pagination is requested. Callers that always
  // send page/size (e.g. TaskListComponent) can safely treat the result as TaskPage.
  list(options: TaskListOptions = {}): Observable<Task[] | TaskPage> {
    let params = new HttpParams();
    for (const p of options.priority ?? []) {
      params = params.append('priority', p);
    }
    if (options.status) {
      params = params.set('status', options.status);
    }
    if (options.sort) {
      params = params.set('sort', options.sort);
    }
    if (options.page !== undefined) {
      params = params.set('page', options.page);
    }
    if (options.size !== undefined) {
      params = params.set('size', options.size);
    }
    return this.http.get<Task[] | TaskPage>(this.baseUrl, { params });
  }

  create(
    title: string,
    description: string,
    dueDate: string | null,
  ): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, { title, description, dueDate });
  }

  update(
    id: number,
    title: string,
    description: string,
    dueDate: string | null,
  ): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, {
      title,
      description,
      dueDate,
    });
  }

  complete(id: number): Observable<Task> {
    return this.http.patch<Task>(`${this.baseUrl}/${id}/complete`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
