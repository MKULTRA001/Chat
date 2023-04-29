function lightmode_init(){
    let Switch = document.getElementById('switch');

    let lightmodeCookie = {
        set:function(key,value,time,path,secure=false) {
            let expires = new Date();
            expires.setTime(expires.getTime() + time);
            var path   = (typeof path !== 'undefined') ? pathValue = 'path=' + path + ';' : '';
            var secure = (secure) ? ';secure' : '';

            document.cookie = key + '=' + value + ';' + path + 'expires=' + expires.toUTCString() + secure;
        },
        get:function() {
            let keyValue = document.cookie.match('(^|;) ?lightmode=([^;]*)(;|$)');
            return keyValue ? keyValue[2] : null;
        },
        remove:function() {
            document.cookie = 'lightmode=; Max-Age=0; path=/';
        }
    };

    if(lightmodeCookie.get() === 'true') {
        document.documentElement.setAttribute('data-theme', 'light');
        Switch.checked = true;
    }

    Switch.addEventListener('click', (event) => {
        if(Switch.checked) {
            document.documentElement.setAttribute('data-theme', 'light');
            lightmodeCookie.set('lightmode','true',2628000000,'/',false);
        }
        else {
            document.documentElement.setAttribute('data-theme', 'dark');
            lightmodeCookie.remove();
        }
    });
}

window.onload = lightmode_init;
