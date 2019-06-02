// Fetch stats and display them in the page.
function fetchStats(){
  const url = '/stats';
  fetch(url).then((response) => {
    return response.json();
  }).then((stats) => {
    const statsContainer = document.getElementById('stats-container');
    statsContainer.innerHTML = '';

    const messageCountElement = buildStatElement('Message count: ' + stats.messageCount);
    const averageMessageLengthElement = buildStatElement('Average message length: ' + stats.averageMessageLength);
    const longestMessage = buildStatElement('Longest Message: ' + stats.longestMessage);
    const userCountElement = buildStatElement('User count: ' + stats.userCount);
    statsContainer.appendChild(messageCountElement);
    statsContainer.appendChild(averageMessageLengthElement);
    statsContainer.appendChild(longestMessage);
    statsContainer.appendChild(userCountElement);
  });
}

function buildStatElement(statString){
 const statElement = document.createElement('p');
 statElement.appendChild(document.createTextNode(statString));
 return statElement;
}
// Fetch data and populate the UI of the page.
function buildUI(){
 fetchStats();
}