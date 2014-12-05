CKEDITOR.editorConfig = function(config) {
	config.resize_enabled = false;
	config.toolbar = 'Complex';
	config.height = '520px';
	config.removePlugins = 'elementspath';		
	config.toolbar_Complex = [
	              			[ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript',
	              					'Superscript', 'BulletedList' , 'TextColor', 'BGColor', '-', 'Cut', 'Copy',
	              					'Paste', 'Link', 'Unlink', 'Image'],
	              			[ 'Undo', 'Redo', '-', 'JustifyLeft', 'JustifyCenter',
	              					'JustifyRight', 'JustifyBlock' ],
	              			[ 'Table', 'Smiley', 'SpecialChar', 'PageBreak',
	              					'Styles', 'Format', 'Font', 'FontSize'] ];
};