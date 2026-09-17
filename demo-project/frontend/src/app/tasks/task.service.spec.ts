import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Task, TaskService } from './task.service';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  const baseUrl = 'http://localhost:8080/api/tasks';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskService],
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('sends a PUT request with the updated title, description and due date', () => {
    const updated: Task = {
      id: 1,
      title: 'New title',
      description: 'New description',
      completed: false,
      ownerId: 5,
      createdAt: '2026-01-01T00:00:00Z',
      dueDate: '2026-02-01',
      overdue: false,
    };

    service.update(1, 'New title', 'New description', '2026-02-01').subscribe((task) => {
      expect(task).toEqual(updated);
    });

    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ title: 'New title', description: 'New description', dueDate: '2026-02-01' });
    req.flush(updated);
  });

  it('sends a POST request with the title, description and due date', () => {
    const created: Task = {
      id: 2,
      title: 'Title',
      description: 'Description',
      completed: false,
      ownerId: 5,
      createdAt: '2026-01-01T00:00:00Z',
      dueDate: '2026-03-01',
      overdue: false,
    };

    service.create('Title', 'Description', '2026-03-01').subscribe((task) => {
      expect(task).toEqual(created);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'Title', description: 'Description', dueDate: '2026-03-01' });
    req.flush(created);
  });
});
