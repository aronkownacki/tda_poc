var seriesOptions = [],
    seriesCounter = 0,
    names = [],
    mainChart;

$.when(
    $.getJSON('http://localhost:10400/report/metadata/countries', function (data) {
        names = data;
    })
).then(function(){
    $.each(names, function (i, name) {

        $.getJSON('http://localhost:10400/report/' + name.toLowerCase(), function (data) {

            seriesOptions[i] = {
                name: name,
                data: data
            };

            // As we're loading the data asynchronously, we don't know what order it will arrive. So
            // we keep a counter and create the chart when all the data is loaded.
            seriesCounter += 1;

            if (seriesCounter === names.length) {
                createChart();
            }
        });
    });

});

function createChart() {

    mainChart = Highcharts.stockChart('container', {

//        chart: {
//            events: {
//                load: function () {
//
////                    // set up the updating of the chart each second
////                    var series = this.series[0];
////                    setInterval(function () {
////                        var x = (new Date()).getTime(), // current time
////                            y = Math.round(Math.random() * 100);
////                        series.addPoint([x, y], false, true);
////                    }, 1000);
//                    // set up the updating of the chart each second
//
//                    setInterval(function () {
//// this.series[0].data[0].update(y += 1);
//                    }, 1000);
//                }
//            }
//        },

        chart: {
            borderWidth: 2,
            height: 400 + (seriesCounter * 35)
        },

        title:{
			text: "Eurosceptic barometer"
		},

        rangeSelector: {
            selected: 4
        },

        yAxis: {
            labels: {
                formatter: function () {
                    return (this.value > 0 ? ' + ' : '') + this.value + ' votes';
                }
            },
            plotLines: [{
                value: 0,
                width: 2,
                color: 'silver'
            }]
        },

//        plotOptions: {
//            series: {
//                compare: 'value',
//                showInNavigator: true
//            }
//        },

        tooltip: {
            pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>',
            valueDecimals: 2,
            split: true
        },

        series: seriesOptions
    });
};

var y = 30;
$('#button').click(function () {
    y += 10
    mainChart.series[0].data[0].update(y);
    mainChart.redraw();
});
