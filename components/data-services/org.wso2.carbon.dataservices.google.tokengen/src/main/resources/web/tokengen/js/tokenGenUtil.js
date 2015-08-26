function reDirectToConsent() {
    var data = getData();
    $.ajax({
               url: '/consentUrl',
               type: 'POST',
               async: false,
               cache: false,
               data: JSON.stringify(data),
               processData: false,
               timeout: 5000,
               error: function () {
                   status = true;
               },
               contentType: 'application/json',
               statusCode: {
                   400: function (response) {
                       alert(response.responseText);
                   },
                   500: function (response) {
                       alert(response.responseText);
                   }
               },
               success: function (msg) {
                   msg = msg.toString().trim();
                   OpenInNewTab(msg);
                   setTimeout(getStatus, 5000);
               }
           });

    return false;
}

function getData() {
    var data = {};
    $("#clientDataTable").find(":text").each(function () {
        var key = $(this).attr('id');
        var val = $(this).val();
        data[key] = val;
    });
    return data;
}

function OpenInNewTab(url) {
    var win = window.open(url, '_blank');
    win.focus();
}

function setTokensToPage(jsonObj) {
    $("#tokenTable").css("display", "");
    $("#tokenTable").find(":text").each(function () {
        var key = $(this).attr('id');
        if (jsonObj.hasOwnProperty(key)) {
            $(this).val(jsonObj[key]);
        }
    });
}

function getStatus() {
    var data = getData();
    $.ajax({
               url: '/tokenEndpoint',
               type: 'POST',
               async: false,
               cache: false,
               data: JSON.stringify(data),
               processData: false,
               timeout: 5000,
               error: function () {
                   status = true;
               },
               contentType: 'application/json',
               statusCode: {
                   400: function (response) {
                       alert(response.responseText);
                   },
                   500: function (response) {
                       alert(response.responseText);
                   }
               },
               success: function (msg) {
                   msg = msg.toString().trim();
//                   msg = msg.replace(/(\r\n|\n|\r)/gm, "");
                   var obj = jQuery.parseJSON(msg);
                   if (!obj['gspread_access_token']) {
                       setTimeout(getStatus, 5000);
                   } else {
                       setTokensToPage(obj)
                   }
               }
           });
}