$.mockjax({
  url: '/xtest/mock3',
  status: 500,
  statusText: 'Server error'
});


var ret;

$.ajax({
    url: '/xtest/mock3',
    type: "GET",
    cache: false,
    dataType: "json",
    error: function(qXHR, textStatus, errorThrown) {
        ret = { status: textStatus, code: qXHR.status, message: qXHR.statusText };
    }
});

