function openVideo(videoPath) {

    const popup = document.getElementById("videoPopup");
    const video = document.getElementById("projectVideo");

    video.src = videoPath;

    popup.style.display = "flex";

    video.play();
}


function closeVideo() {

    const popup = document.getElementById("videoPopup");
    const video = document.getElementById("projectVideo");

    video.pause();

    video.currentTime = 0;

    video.src = "";

    popup.style.display = "none";
}

// ========================================
// SCROLL REVEAL ANIMATION
// ========================================

document.addEventListener("DOMContentLoaded", function () {

    const revealElements = document.querySelectorAll(
        ".about, .section-heading, .project-card, .contact"
    );

    revealElements.forEach(function (element) {
        element.classList.add("reveal");
    });

    const observer = new IntersectionObserver(
        function (entries) {

            entries.forEach(function (entry) {

                if (entry.isIntersecting) {
                    entry.target.classList.add("active");
                    observer.unobserve(entry.target);
                }

            });

        },
        {
            threshold: 0.15
        }
    );

    revealElements.forEach(function (element) {
        observer.observe(element);
    });

});