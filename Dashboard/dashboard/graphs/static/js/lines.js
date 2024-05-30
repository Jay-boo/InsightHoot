let myLineChart;

// Predefined colors for themes
const themeColors = {
    'langages': '#ffbe0b',
    'IA': '#fb5607',
    'OS': '#ff006e',
    // Add more themes and their corresponding colors here
    'default': '#000000' // Default color if theme is not specified
};

function aggregateDataByMonthAndTheme(data) {
    const themeCountsByMonth = {};

    data.forEach(item => {
        const date = new Date(item.date);
        const month = date.getFullYear() + '-' + (date.getMonth() + 1).toString().padStart(2, '0');

        item.tags.forEach(tagItem => {
            const theme = tagItem.tag.theme;

            if (!themeCountsByMonth[month]) {
                themeCountsByMonth[month] = {};
            }
            if (!themeCountsByMonth[month][theme]) {
                themeCountsByMonth[month][theme] = 0;
            }
            themeCountsByMonth[month][theme] += 1;
        });
    });

    return themeCountsByMonth;
}

function prepareLineChartData(themeCountsByMonth) {
    const months = Object.keys(themeCountsByMonth).sort();
    const themes = [...new Set(Object.values(themeCountsByMonth).flatMap(Object.keys))];

    const datasets = themes.map(theme => {
        return {
            label: theme,
            data: months.map(month => themeCountsByMonth[month][theme] || 0),
            fill: false,
            borderColor: themeColors[theme] || themeColors['default']
        };
    });

    return { months, datasets };
}

function renderLineChart(data) {
    const aggregatedData = aggregateDataByMonthAndTheme(data);
    const { months, datasets } = prepareLineChartData(aggregatedData);
    const ctx = document.getElementById('myLineChart').getContext('2d');
    console.log(months);
    console.log(datasets);
    if (myLineChart) {
        myLineChart.destroy();
    }

    myLineChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: months,
            datasets: datasets
        },
        options: {
            responsive: true,
            title: {
                display: true,
                text: 'Number of Messages per Theme per Month'
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    return myLineChart;
}

function fetchDataBasedOnSelection(selection) {
    let url = '/graphs/messages_with_tags/';
    if (selection === 'Les 7 derniers jours') {
        url += '?period=last_7_days';
    } else if (selection === 'Le dernier mois') {
        url += '?period=last_month';
    } else if (selection === 'Les 3 derniers mois') {
        url += '?period=last_3_months';
    } else if (selection === 'Les 6 derniers mois') {
        url += '?period=last_6_months';
    }
    fetch(url)
        .then(response => response.json())
        .then(data => {
            renderLineChart(data);
        })
        .catch(error => console.error('Error fetching data:', error));
}

document.getElementById('Dropdown').addEventListener('change', (event) => {
    fetchDataBasedOnSelection(event.target.value);
});

// Fetch data from the endpoint and render the chart initially
fetch('/graphs/messages_with_tags/')
    .then(response => response.json())
    .then(data => {
        renderLineChart(data);
    })
    .catch(error => console.error('Error fetching data:', error));

