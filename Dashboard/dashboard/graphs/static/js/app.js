let myPieChart = null;
Chart.defaults.elements.arc.borderWidth = 0;
Chart.defaults.elements.arc.hoverOffset = 15;
// Function to aggregate data by theme
function aggregateDataByTheme(data) {
    const themeCounts = {};

    data.forEach(item => {
        item.tags.forEach(tagItem => {
            const theme = tagItem.tag.theme;
            if (theme in themeCounts) {
                themeCounts[theme]++;
            } else {
                themeCounts[theme] = 1;
            }
        });
    });

    return themeCounts;
}



// Function to aggregate data by labels within a theme
function aggregateDataByLabels(data) {
    const themeCounts = {};

    data.forEach(item => {
        item.tags.forEach(tagItem => {
            const theme = tagItem.tag.label;
            if (theme in themeCounts) {
                themeCounts[theme]++;
            } else {
                themeCounts[theme] = 1;
            }
        });
    });

    return themeCounts;
}

// Function to render the pie chart using Chart.js
function renderPieChart(data, theme = null) {
    let aggregatedData, chartTitle;

    if (theme) {
        aggregatedData = aggregateDataByLabels(data, theme);
        chartTitle = `Distribution des labels dans le thème "${theme}"`;
    } else {
        aggregatedData = aggregateDataByTheme(data);
        chartTitle = 'Distribution des thèmes';
    }

    const labels = Object.keys(aggregatedData);
    const values = Object.values(aggregatedData);

    const ctx = document.getElementById('myPieChart').getContext('2d');

    if (myPieChart) {
        myPieChart.destroy();
    }

    myPieChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: ['#845ec2', '#2c73d2', '#0081cf', '#0089ba', '#008e9b', '#008f7a'],
            }]
        },
        options: {
            responsive: true,
            plugins: {
                datalabels: {
                    color: '#fff',
                    formatter: function(value, context) {
                        return context.chart.data.labels[context.dataIndex];
                    },
                    font: {
                        weight: 'bold',
                        size: 16
                    }
                },
                title: {
                    display: true,
                    text: chartTitle,
                    color: '#000000',
                    font: {
                        family: "'Geneva','Tahoma','Verdana',sans-serif",
                        size: 18,
                        weight: 'normal'
                    }
                },
                legend: {
                  display: false
                }
            },
            onClick: (event, elements) => {
                if (elements.length > 0) {
                    const chartElement = elements[0];
                    const label = chartElement.element.$context.raw;
                    if (!theme) {
                        renderPieChart(data, label);
                    }
                }
            }
        },
        plugins: [ChartDataLabels]
    });

    return myPieChart;
}

// Fetch data from the endpoint and render the chart
fetch('/graphs/messages_with_tags/')
    .then(response => response.json())
    .then(data => {
        renderPieChart(data);
    })
    .catch(error => console.error('Error fetching data:', error));

