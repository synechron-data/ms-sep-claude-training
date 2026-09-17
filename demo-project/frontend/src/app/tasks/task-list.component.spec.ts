import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TaskListComponent } from './task-list.component';
import { Task, TaskService } from './task.service';

describe('TaskListComponent', () => {
  let fixture: ComponentFixture<TaskListComponent>;
  let component: TaskListComponent;
  let taskService: jasmine.SpyObj<TaskService>;

  const task: Task = {
    id: 1,
    title: 'Original title',
    description: 'Original description',
    completed: false,
    ownerId: 5,
    createdAt: '2026-01-01T00:00:00Z',
    dueDate: '2026-01-15',
    overdue: false,
    priority: 'MEDIUM',
  };

  beforeEach(async () => {
    taskService = jasmine.createSpyObj('TaskService', ['list', 'create', 'complete', 'delete', 'update']);
    taskService.list.and.returnValue(of([task]));

    await TestBed.configureTestingModule({
      imports: [TaskListComponent],
      providers: [{ provide: TaskService, useValue: taskService }],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('populates edit fields when editing starts', () => {
    component.startEdit(task);

    expect(component.editingTaskId).toBe(task.id);
    expect(component.editTitle).toBe(task.title);
    expect(component.editDescription).toBe(task.description);
    expect(component.editDueDate).toBe(task.dueDate as string);
  });

  it('calls TaskService.update with the edited values and refreshes the list', () => {
    taskService.update.and.returnValue(of({ ...task, title: 'Updated title', description: 'Updated description' }));

    component.startEdit(task);
    component.editTitle = 'Updated title';
    component.editDescription = 'Updated description';
    component.editDueDate = '2026-02-01';
    component.saveEdit(task);

    expect(taskService.update).toHaveBeenCalledWith(task.id, 'Updated title', 'Updated description', '2026-02-01');
    expect(component.editingTaskId).toBeNull();
    expect(taskService.list).toHaveBeenCalledTimes(2);
  });

  it('does not call TaskService.update when the edited title is blank', () => {
    component.startEdit(task);
    component.editTitle = '   ';
    component.saveEdit(task);

    expect(taskService.update).not.toHaveBeenCalled();
  });

  it('clears editing state on cancel without calling TaskService.update', () => {
    component.startEdit(task);
    component.cancelEdit();

    expect(component.editingTaskId).toBeNull();
    expect(taskService.update).not.toHaveBeenCalled();
  });

  it('applies the overdue class to an overdue task', () => {
    taskService.list.and.returnValue(of([{ ...task, overdue: true }]));
    component.refresh();
    fixture.detectChanges();

    const li: HTMLElement = fixture.nativeElement.querySelector('li');

    expect(li.classList).toContain('overdue');
  });

  it('does not apply the overdue class to a non-overdue task', () => {
    taskService.list.and.returnValue(of([{ ...task, overdue: false }]));
    component.refresh();
    fixture.detectChanges();

    const li: HTMLElement = fixture.nativeElement.querySelector('li');

    expect(li.classList).not.toContain('overdue');
  });
});
