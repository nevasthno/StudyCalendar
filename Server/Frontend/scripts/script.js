const tasks = [
    { id: 1, title: 'Домашнє завдання з математики', completed: false },
    { id: 2, title: 'Підготуватися до екзамену з історії', completed: false },
    { id: 3, title: 'Перегляд шкільного заходу', completed: true }
];

const calendarData = [
    { date: '2025-02-17', event: 'Контрольна робота з біології' },
    { date: '2025-02-18', event: 'Батьківські збори' },
    { date: '2025-02-20', event: 'Екзамен з фізики' }
];

document.addEventListener('DOMContentLoaded', () => {
    loadTasks();
    loadCalendar();
});


function loadTasks() {
    const taskList = document.getElementById('tasks-list');
    taskList.innerHTML = '';

    tasks.forEach(task => {
        const li = document.createElement('li');
        li.textContent = task.title;
        if (task.completed) {
            li.classList.add('completed');
        }
        taskList.appendChild(li);
    });
}


function loadCalendar() {
    const today = new Date();
    let currentMonth = today.getMonth();
    let currentYear = today.getFullYear();

    updateCalendar(currentMonth, currentYear);


    window.changeMonth = (offset) => {
        currentMonth += offset;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        } else if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        updateCalendar(currentMonth, currentYear);
    };
}


function updateCalendar(month, year) {
    const monthName = new Intl.DateTimeFormat('uk', { month: 'long' }).format(new Date(year, month));
    document.getElementById('month-name').textContent = `${monthName} ${year}`;

    const calendarBody = document.getElementById('calendar-body');
    calendarBody.innerHTML = '';

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    let date = 1;

    for (let i = 0; i < 6; i++) {
        const row = document.createElement('tr');

        for (let j = 0; j < 7; j++) {
            const cell = document.createElement('td');
            if (i === 0 && j < firstDay) {
                cell.textContent = '';
            } else if (date > daysInMonth) {
                break;
            } else {
                cell.textContent = date;
                const fullDate = `${year}-${(month + 1).toString().padStart(2, '0')}-${date.toString().padStart(2, '0')}`;
                const event = calendarData.find(e => e.date === fullDate);
                if (event) {
                    const eventSpan = document.createElement('span');
                    eventSpan.textContent = ` (${event.event})`;
                    cell.appendChild(eventSpan);
                }
                date++;
            }
            row.appendChild(cell);
        }
        calendarBody.appendChild(row);
    }
}
