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

  it('sends a PUT request with the updated title and description', () => {
    const updated: Task = {
      id: 1,
      title: 'New title',
      description: 'New description',
      completed: false,
      ownerId: 5,
      createdAt: '2026-01-01T00:00:00Z',
    };

    service.update(1, 'New title', 'New description').subscribe((task) => {
      expect(task).toEqual(updated);
    });

    const req = httpMock.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ title: 'New title', description: 'New description' });
    req.flush(updated);
  });
});
