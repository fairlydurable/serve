let logIndex = 1;

function addAction() {
    const log = document.getElementById('log');
    const action = document.createElement('div');
    action.textContent = `Action ${logIndex++} performed at ${new Date().toLocaleTimeString()}.`;
    log.appendChild(action);
}
