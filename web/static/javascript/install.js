/* Modify the ajax callback to our own */
form_ajax_callback = install_step;

/* Hook in our install step to put the initial value */
Event.observe(window, 'load', function() {
	install_step();
});

/* Modify the progress bar if we're installing */
function install_step()
{
	var curr_step = $('step').getValue();
	
	if(curr_step > 0)
	{
		set_progress((curr_step - 1) * 25);
	}
}