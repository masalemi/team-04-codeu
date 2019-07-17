jQuery(document).ready(function(){
	var intro = $('.cd-intro-block'),
		projectsContainer = $('.cd-projects-wrapper'),
		projectsSlider = projectsContainer.children('.cd-slider'),
		singleProjectContent = $('.cd-project-content'),
		sliderNav = $('.cd-slider-navigation');

	var resizing = false;

	//if on desktop - set a width for the projectsSlider element
	setSliderContainer();
	$(window).on('resize', function(){
		//on resize - update projectsSlider width and translate value
		if( !resizing ) {
			(!window.requestAnimationFrame) ? setSliderContainer() : window.requestAnimationFrame(setSliderContainer);
			resizing = true;
		}
	});
	showProjectPreview(projectsSlider.children('li').eq(0));

	//go to next/pre slide - clicking on the next/prev arrow
	sliderNav.on('click', '.next', function(){
		var mq = checkMQ();
		if( !(sliderNav.find('.next').hasClass('inactive')) && (mq == 'desktop') ) nextSides(projectsSlider);
	});
	sliderNav.on('click', '.prev', function(){
		var mq = checkMQ();
		if ( !(sliderNav.find('.prev').hasClass('inactive')) && (mq == 'desktop') ) prevSides(projectsSlider);
	});

	projectsSlider.on('swipeleft', function(){
		var mq = checkMQ();
		if( !(sliderNav.find('.next').hasClass('inactive')) && (mq == 'desktop') ) nextSides(projectsSlider);
	});

	projectsSlider.on('swiperight', function(){
		var mq = checkMQ();
		if ( !(sliderNav.find('.prev').hasClass('inactive')) && (mq == 'desktop') ) prevSides(projectsSlider);
	});

	function showProjectPreview(project) {
		if(project.length > 0 ) {
			setTimeout(function(){
				project.addClass('slides-in');
				showProjectPreview(project.next());
			}, 50);
		}
	}

	function checkMQ() {
		//check if mobile or desktop device
		return window.getComputedStyle(document.querySelector('.cd-projects-wrapper'), '::before').getPropertyValue('content').replace(/'/g, "").replace(/"/g, "");
	}

	function setSliderContainer() {
		var mq = checkMQ();
		if(mq == 'desktop') {
			var	slides = projectsSlider.children('li'),
				slideWidth = slides.eq(0).width(),
				marginLeft = Number(projectsSlider.children('li').eq(1).css('margin-left').replace('px', '')),
				sliderWidth = ( slideWidth + marginLeft )*( slides.length + 1 ) + 'px',
				slideCurrentIndex = projectsSlider.children('li.current').index();
			projectsSlider.css('width', sliderWidth);
			( slideCurrentIndex != 0 ) && setTranslateValue(projectsSlider, (  slideCurrentIndex * (slideWidth + marginLeft) + 'px'));
		} else {
			projectsSlider.css('width', '');
			setTranslateValue(projectsSlider, 0);
		}
		resizing = false;
	}

	function nextSides(slider) {
		var actual = slider.children('.current'),
			index = actual.index(),
			following = actual.nextAll('li').length,
			width = actual.width(),
			marginLeft = Number(slider.children('li').eq(1).css('margin-left').replace('px', ''));

		index = (following > 4 ) ? index + 3 : index + following - 2;
		//calculate the translate value of the slider container
		translate = index * (width + marginLeft) + 'px';

		slider.addClass('next');
		setTranslateValue(slider, translate);
		slider.one('webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend', function(){
			updateSlider('next', actual, slider, following);
		});

		if( $('.no-csstransitions').length > 0 ) updateSlider('next', actual, slider, following);
	}

	function prevSides(slider) {
		var actual = slider.children('.previous'),
			index = actual.index(),
			width = actual.width(),
			marginLeft = Number(slider.children('li').eq(1).css('margin-left').replace('px', ''));

		translate = index * (width + marginLeft) + 'px';

		slider.addClass('prev');
		setTranslateValue(slider, translate);
		slider.one('webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend', function(){
			updateSlider('prev', actual, slider);
		});

		if( $('.no-csstransitions').length > 0 ) updateSlider('prev', actual, slider);
	}

	function updateSlider(direction, actual, slider, numerFollowing) {
		if( direction == 'next' ) {

			slider.removeClass('next').find('.previous').removeClass('previous');
			actual.removeClass('current');
			if( numerFollowing > 4 ) {
				actual.addClass('previous').next('li').next('li').next('li').addClass('current');
			} else if ( numerFollowing == 4 ) {
				actual.next('li').next('li').addClass('current').prev('li').prev('li').addClass('previous');
			} else {
				actual.next('li').addClass('current').end().addClass('previous');
			}
		} else {

			slider.removeClass('prev').find('.current').removeClass('current');
			actual.removeClass('previous').addClass('current');
			if(actual.prevAll('li').length > 2 ) {
				actual.prev('li').prev('li').prev('li').addClass('previous');
			} else {
				( !slider.children('li').eq(0).hasClass('current') ) && slider.children('li').eq(0).addClass('previous');
			}
		}

		updateNavigation();
	}

	function updateNavigation() {
		//update visibility of next/prev buttons according to the visible slides
		var current = projectsContainer.find('li.current');
		(current.is(':first-child')) ? sliderNav.find('.prev').addClass('inactive') : sliderNav.find('.prev').removeClass('inactive');
		(current.nextAll('li').length < 3 ) ? sliderNav.find('.next').addClass('inactive') : sliderNav.find('.next').removeClass('inactive');
	}

	function setTranslateValue(item, translate) {
		item.css({
		    '-moz-transform': 'translateX(-' + translate + ')',
		    '-webkit-transform': 'translateX(-' + translate + ')',
			'-ms-transform': 'translateX(-' + translate + ')',
			'-o-transform': 'translateX(-' + translate + ')',
			'transform': 'translateX(-' + translate + ')',
		});
	}
});


$(document).ready(function(){
  /* 1. Visualizing things on Hover - See next part for action on click */
  $('#stars li').on('mouseover', function(){
    var onStar = parseInt($(this).data('value'), 10); // The star currently mouse on

    // Now highlight all the stars that's not after the current hovered star
    $(this).parent().children('li.star').each(function(e){
      if (e < onStar) {
        $(this).addClass('hover');
      }
      else {
        $(this).removeClass('hover');
      }
    });

  }).on('mouseout', function(){
    $(this).parent().children('li.star').each(function(e){
      $(this).removeClass('hover');
    });
  });

  /* 2. Action to perform on click */
  $('#stars li').on('click', function(){
    var onStar = parseInt($(this).data('value'), 10); // The star currently selected
    var stars = $(this).parent().children('li.star');

    for (i = 0; i < stars.length; i++) {
      $(stars[i]).removeClass('selected');
    }

    for (i = 0; i < onStar; i++) {
      $(stars[i]).addClass('selected');
    }
    // JUST RESPONSE (Not needed)
    var ratingValue = parseInt($('#stars li.selected').last().data('value'), 10);
    var msg = "Thanks! You rated this " + ratingValue + " stars.";
		document.getElementById('reviewScoreId').value=ratingValue;
    responseMessage(msg);
	});
});


function responseMessage(msg) {
  $('.success-box').fadeIn(200);
  $('.success-box div.text-message').html("<span>" + msg + "</span>");
}
