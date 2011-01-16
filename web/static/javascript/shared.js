
/* Convenient constants */
var CONTAINER_FADE_DURATION = 0.7;
var form = null;
var form_ajax_callback = function() {};

Event.observe(window, 'load', function() {
	/* Start Fancy Form */
	FancyForm.start();
	
	/* Create our form events */
	create_form_events();
});

function do_ajax(form_obj)
{
	form = form_obj;
	
	/* Hide the submit button */
	$('submit').hide();
	
	/* Fade the form */
	do_fade_submit();
	
	/* Finish the form request */
	setTimeout('_finish_form_request();', CONTAINER_FADE_DURATION * 1000);
	
	return false;
}

function create_form_events()
{
	$$('.form').invoke('observe', 'submit', function()
	{
		element.observe('submit', function(event) {
			/* Stop the event, we're using Ajax */
			event.stop();
			
			$('submit').hide();
			
			/* Fade the form */
			do_fade_submit();
		});
	});
}

function _finish_form_request()
{
	form.request({
		onFailure: function(o) {
			$('content').update(o.responseText);
			$('content').show();
			
			form_ajax_callback();
		},
		onSuccess: function(o) {
			$('content').update(o.responseText);
			$('content').show();
			
			form_ajax_callback();
		}
	});
}

function set_progress(progress)
{
	$$('.ui-progress').invoke('setStyle', {
		width: progress + '%'
	});
	$$('.value').invoke('update', progress + "%");
}

function do_fade_submit()
{
	new Effect.Fade( 'content', { duration: CONTAINER_FADE_DURATION });
}

function sleep(milliseconds)
{
    var start = new Date().getTime();   
       
    for (var i = 0; i < 1e7; i++) {   
        if ((new Date().getTime() - start) > milliseconds) {   
            break;   
        }   
    }   
}