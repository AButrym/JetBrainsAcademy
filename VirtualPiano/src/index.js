document.addEventListener('keydown', function(e) {
    const key = e.key.toUpperCase();
    if ("ASDFGHJWETYU".includes(key)) {
        new Audio(`./audio/${key}.mp3`).play();
    }
})