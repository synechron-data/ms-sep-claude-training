import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { TaskListComponent } from './task-list.component';
import { Task, TaskPage, TaskService } from './task.service';

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

  function pageOf(items: Task[], overrides: Partial<TaskPage> = {}): TaskPage {
    return {
      items,
      page: 0,
      size: 20,
      totalElements: items.length,
      totalPages: 1,
      ...overrides,
    };
  }

  beforeEach(async () => {
    taskService = jasmine.createSpyObj('TaskService', [
      'list',
      'create',
      'complete',
      'delete',
      'update',
    ]);
    taskService.list.and.returnValue(of(pageOf([task])));

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
    taskService.update.and.returnValue(
      of({
        ...task,
        title: 'Updated title',
        description: 'Updated description',
      }),
    );

    component.startEdit(task);
    component.editTitle = 'Updated title';
    component.editDescription = 'Updated description';
    component.editDueDate = '2026-02-01';
    component.saveEdit(task);

    expect(taskService.update).toHaveBeenCalledWith(
      task.id,
      'Updated title',
      'Updated description',
      '2026-02-01',
    );
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
    taskService.list.and.returnValue(of(pageOf([{ ...task, overdue: true }])));
    component.refresh();
    fixture.detectChanges();

    const li: HTMLElement = fixture.nativeElement.querySelector('li');

    expect(li.classList).toContain('overdue');
  });

  it('does not apply the overdue class to a non-overdue task', () => {
    taskService.list.and.returnValue(of(pageOf([{ ...task, overdue: false }])));
    component.refresh();
    fixture.detectChanges();

    const li: HTMLElement = fixture.nativeElement.querySelector('li');

    expect(li.classList).not.toContain('overdue');
  });

  it('requests page 0 on load', () => {
    expect(taskService.list).toHaveBeenCalledWith(
      jasmine.objectContaining({ page: 0, size: 20 }),
    );
  });

  it('goToPage requests the new page and keeps the current filters', () => {
    component.totalPages = 3;
    component.priorityFilter = ['HIGH'];
    component.statusFilter = 'incomplete';
    component.sortByPriority = true;
    taskService.list.calls.reset();
    taskService.list.and.returnValue(of(pageOf([task], { page: 1 })));

    component.goToPage(1);

    expect(component.page).toBe(1);
    expect(taskService.list).toHaveBeenCalledWith({
      page: 1,
      size: 20,
      priority: ['HIGH'],
      status: 'incomplete',
      sort: 'priority',
    });
  });

  it('goToPage does nothing when the target page is out of range', () => {
    component.page = 0;
    component.totalPages = 1;
    taskService.list.calls.reset();

    component.goToPage(1);

    expect(component.page).toBe(0);
    expect(taskService.list).not.toHaveBeenCalled();
  });

  it('togglePriority adds and removes a priority filter and resets to page 0', () => {
    component.page = 2;
    taskService.list.calls.reset();
    taskService.list.and.returnValue(of(pageOf([task])));

    component.togglePriority('HIGH');

    expect(component.priorityFilter).toEqual(['HIGH']);
    expect(component.page).toBe(0);
    expect(taskService.list).toHaveBeenCalledWith(
      jasmine.objectContaining({ priority: ['HIGH'], page: 0 }),
    );

    component.togglePriority('HIGH');

    expect(component.priorityFilter).toEqual([]);
  });

  it('onFilterChange sends status and sort only when set', () => {
    taskService.list.calls.reset();
    component.statusFilter = 'complete';
    component.sortByPriority = true;

    component.onFilterChange();

    expect(taskService.list).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      status: 'complete',
      sort: 'priority',
    });
  });
});
