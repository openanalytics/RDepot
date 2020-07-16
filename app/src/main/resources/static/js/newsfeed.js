var CURRENT_DATE = "";
var LAST_EVENT = 0;
const MONTH_NAMES = ["January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December"
];
const EVENT_TYPE_ICONS = {
    added: "check",
    removed: "delete_forever",
    submitted: "cloud_upload",
    created: "cake",
    edited: "edit",
    deleted: "delete_forever"
};

const EDIT_REPOSITORY_EVENT_TYPES = [
    "publication URI",
    "server address",
    "version",
    "name"
];

$(document).ready(function()
{
    sendNewsfeedUpdateRequest();
});

function sendNewsfeedUpdateRequest()
{

    var request = new XMLHttpRequest();
    var url = "";
    if(CURRENT_DATE != "")
        url = '/manager/newsfeed/update?date=' + CURRENT_DATE + '&lastPosition=' + LAST_EVENT;
    else
        url = '/manager/newsfeed/update';
    var events = {};

    request.onreadystatechange = function() {
        if(this.readyState == 4 && this.status == 200) {
            events = JSON.parse(this.responseText);
            var initialize = (CURRENT_DATE == "" ? true : false);
            updateFeed(events);
            if(initialize)
                initializeMonths();

            var latestEvents = events[CURRENT_DATE];
            if(latestEvents.length > 0) {
                LAST_EVENT = latestEvents[latestEvents.length - 1].id;
            }

        }
    };

    request.open("GET", url, true);
    request.setRequestHeader("Accept", "application/json");
    request.send();
}

function updateFeed(events) {
    var dates = Object.keys(events).sort().reverse();

    var months = {};
    var html = '<dt data-hidden="0" class="month" style="display: none;"><span>June 2015</span></dt>';
    var key, title;
    for(var i = 0; i < dates.length; i++) {
        key = dates[i].split('-')[0] + dates[i].split('-')[1];
        title = MONTH_NAMES[parseInt(dates[i].split('-')[1], 10) - 1] + ' ' + dates[i].split('-')[0];
        months[key] = title;
    }

    var timeline = document.getElementsByClassName("timeline")[0].getElementsByTagName("dl")[0];


    var monthsNumeric = Object.keys(months).sort().reverse();
    for(var i = 0; i < monthsNumeric.length; i++) {
        if(document.getElementById("month_" + monthsNumeric[i]) === null) {
            var monthEntry = document.createElement('div');
            monthEntry.classList.add('month-container');
            monthEntry.id = "month_" + monthsNumeric[i];
            var html = '<dt data-hidden="0" class="month"><span>' + months[monthsNumeric[i]] + '</span></dt>';
            monthEntry.innerHTML = html;

            if(CURRENT_DATE != "") {
                timeline.insertBefore(monthEntry, timeline.getElementsByClassName("month-container")[0]);
            } else {
                timeline.appendChild(monthEntry);
            }
        }
    }

    for(var i = 0; i < dates.length; i++) {
        var hidden = (i > 0 ? true : false);
        var month = dates[i].split('-')[0] + dates[i].split('-')[1];
        var monthContainer = document.getElementById("month_" + month);
        var day = dates[i].split('-')[2];

        if(document.getElementById("day_" + month + day) === null) {
            var dayEntry = document.createElement('div');
            dayEntry.classList.add("day-container");
            dayEntry.id = "day_" + month + day;
            var html = '<dt class="day"><span>' + day + ' ' + months[month] + '</span></dt>';
            dayEntry.innerHTML = html;

            if(CURRENT_DATE != "") {
                monthContainer.insertBefore(dayEntry, monthContainer.getElementsByClassName("day-container")[0]);
            } else {
                monthContainer.appendChild(dayEntry);
            }
        }
    }
    for(var i = 0; i < dates.length; i++) {
        var id = 'day_' + dates[i].split('-')[0] + dates[i].split('-')[1] + dates[i].split('-')[2];
        var eventsOfADay = events[dates[i]].reverse();
        var newDirection = $("dd:first").hasClass("pos-left") ? "pos-right" : "pos-left";

        for(var k = 0; k < eventsOfADay.length; k++) {
            var eventType = "";
            var objectName = "";
            var describeChange = false;
            
            if(EDIT_REPOSITORY_EVENT_TYPES.includes(eventsOfADay[k].changedVariable)) {
                eventType = "edited (" + eventsOfADay[k].changedVariable + ")";
                objectName = eventsOfADay[k].repositoryName;
                describeChange = true;
            } else {
                eventType = eventsOfADay[k].changedVariable;
                objectName = eventsOfADay[k].valueAfter;
            }
            
            var html = '<dd class="' + newDirection + ' clearfix" style="display:none;"><div class="circ"><i class="material-icons">';
            html += EVENT_TYPE_ICONS[eventType.split(' ')[0]] + '</i></div>';
            html += '<div class="time">' + eventsOfADay[k].time.split('-')[0] + ':' + eventsOfADay[k].time.split('-')[1];
            html += '</div><div class="events"><div class="events-body">';
            
            html += '<h4 class="events-heading"><strong>' + objectName + '</strong> was ' + eventType + '</h4>';
           
            if (objectName != eventsOfADay[k].repositoryName) {
                html += '<div>to <strong>' + eventsOfADay[k].repositoryName + '</strong></div>';
            }
            
            
            html += '<div>by <strong>'+ eventsOfADay[k].maintainer;
            if(describeChange)
            	html += '</strong></div><p>from <em>' + eventsOfADay[k].valueBefore + '</em> to <em>'+ eventsOfADay[k].valueAfter +'</em></p></div></div></dd>';
            $("#" + id + " .day").after(html);
            $($("dt:first")[0]).show(300);
            $($("dt:first")[0]).nextUntil("dt.month").show(300);
            if(i == 0) {
                $($("#" + id + " dd:first")[0]).show(300);
            }
            if(newDirection == "pos-right") {
                newDirection = "pos-left";
            } else {
                newDirection = "pos-right";
            }
        }
    }
    CURRENT_DATE = dates[0];
}

function initializeMonths()
{
    $("dt.day span").click(function()
    {
        $(this).parent().nextUntil("dt").toggle(300);
    });
    $("dt.month span").click(function()
    {
        var hidden = $(this).parent().attr("data-hidden") == "0" ? false : true;
        if(hidden) { $(this).parent().nextUntil("dt.month").slideDown(300); $(this).parent().attr("data-hidden", "0"); }
        else { $(this).parent().nextUntil("dt.month").slideUp(300); $(this).parent().attr("data-hidden", "1"); }
    });
}
