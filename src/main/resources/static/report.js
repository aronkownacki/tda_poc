var seriesOptions = [],
    seriesCounter = 0,
    names = [],
    mainChart;

function showChartScreen() {
$.when(
    $.getJSON('/report/metadata/countries', function (data) {
        names = data;
    })
).then(function(){
    $.each(names, function (i, name) {

// 24 hours period
//        $.getJSON('/report/' + name.toLowerCase() + '/24', function (data) {
//        $.getJSON('/report/' + name.toLowerCase() + '/24/MILLISECOND', function (data) {
// full report
        $.getJSON('/report/' + name.toLowerCase(), function (data) {

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
};

function createChart() {

    mainChart = Highcharts.stockChart('chartContainer', {

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
            type: 'area',
            borderWidth: 2,
            height: 600 + (seriesCounter * 35)
        },

        title:{
			text: "Eurosceptic barometer"
		},

        rangeSelector: {
            selected: 4
        },

        xAxis: {
            ordinal: false
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

        plotOptions: {
            area: {
                stacking: 'normal',
                lineColor: '#666666',
                lineWidth: 1,
                marker: {
                    lineWidth: 1,
                    lineColor: '#666666'
                }
            }
        },

        tooltip: {
            pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>',
            valueDecimals: 2,
            split: true
        },

        series: seriesOptions
    });
};



