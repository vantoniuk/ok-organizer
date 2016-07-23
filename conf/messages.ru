
################################################################################################
# COMMON

field.firstname=Имя
field.lastname=Фамилия
field.nick=Ник
field.email=Электронная почта
field.password=Пароль
field.password.repeat=Повторите пароль
field.role=Роль

submit=отправить
reset=сбросить
back=назад
go.index=на главную

################################################################################################
# MENU

index.page=Главная
topbar.myaccount=Мой аккаунт
topbar.hello=Здравствуйте {0}!<br>Ваши полномочия: {1}

################################################################################################
# EDITOR
created.by=Создана
display.order=Порядок
url=Адрес ссылки
title=Титул
editor.new=Добавить новый
editor.submit=Подтвердить

################################################################################################
# INDEX

index.title=Добро пожаловать
index.intro.logged=Вы вошли как <span class="text-success">{0}</span>, так что вы можете пойти в \
<a href="{1}">Мой аккаунт</a> чтобы увидеть контактную информацию.<br>\
Конечно вы можете <a href="{2}">выйти</a>.
index.intro.notlogged=Now, you aren''t logged, so you can try to <a href="{0}">sign in</a> or <a href="{1}">sign up</a> and create your own account.<br>\
If you don''t remember your password, you also can <a href="{2}">reset your password</a> with the traditional email mechanism.
index.explanation=Each user has one or more services that indicate a specific area or hierarchical level.<br>\
You can restrict sections to those users who match with a set of services (using logic OR or AND, you can choose).<br>\
The <span class="text-warning">master</span> role has always full access to everywhere.
index.example=Por ejemplo:
index.example.serviceA=el usuario tiene acceso al área del ''serviceA''.
index.example.serviceA_serviceB=el usuario tiene acceso a las áreas de ''service A'' and ''service B''.
index.example.master=acceso total a cualquier punto de la página web
index.auth.status=En este caso, estás registrado como {0} y tus servicios son: {1}
index.table.section=Sección
index.table.authobject=Objeto Authorization
index.table.services=Servicios requeridos
index.table.allowed=¿Estás autorizado?
index.table.go=Ve y compruébalo por tí mismo

################################################################################################
# MY ACCOUNT

myaccount.title=Mi cuenta

################################################################################################
# AUTH

signup=Регистрация
signup.title=Создать новый аккаунт
signup.signin.question=Есть аккаунт?
signup.signin=Войти
signup.thanks=Спасибо {0} за регистрацию!
signup.sent=Мы послали письмо на {0}. Пожалуйста следуйте инструкциям чтобы закончить регистрацию.
signup.ready=Ваш аккаунт готов

signin=Войти
signin.title=Войти с вашими данными
signin.rememberme=Запомнить
signin.signup.question=Нет аккаунта?
signin.signup=Зарегистрироваться
signin.forgot.question=Забыли пароль?
signin.forgot=Сбросить пароль

signout=Выйти

forgot.title=Забыли пароль?
forgot.sent=Acabamos de enviarte un email a {0} con las instrucciones para restablecer tu contraseña
forgot.reset.title=Restablece tu contraseña
forgot.reseted=Tu contraseña ha sido restablecida

changepass=Modifica tu contraseña
changepass.title=Modifica tu contraseña
changepass.field.current=Contraseña actual
changepass.field.new=Nueva contraseña
changepass.field.repeat=Repite la contraseña

auth.user.notexists=No hay ningún usuario con este email
auth.user.notunique=No existe otro usuario con este email
auth.credentials.incorrect=Tu email o contraseña son incorrectos
auth.passwords.notequal=Las contraseñas deben ser iguales
auth.password.changed=La contraseña se ha moficicado correctamente
auth.currentpwd.incorrect=La contraseña actual es incorrecta

denied.title=¡Acceso denegado!
denied.text=No tienes autorización para estar aquí.

################################################################################################
# ERRORS

error.unknown.title=Oops, ha ocurrido un error
error.unknown.text=Esta excepción ha sido registrada con el id <strong>{0}</strong>.
error.notfound.title=Dirección no encontrada
error.notfound.text=Para la petición ''{0}''

################################################################################################
# MAILS

mail.welcome.subject=Bienvenido a MyWeb! Por favor, confirma tu cuenta
mail.welcome.hello=¡Bienvenido {0}!
mail.welcome.prelink=¡Gracias por registrate en esta magnífica página web! Por favor, verifica tu dirección de correo pinchando en el siguiente link.
mail.welcome.postlink=Este link expirará en 24 horas si no es activado.

mail.forgotpwd.subject=Restablece tu contraseña para MyWeb
mail.forgotpwd.prelink=Alguien (esperemos que tú) ha solicitado restablecer la contraseña de tu cuenta en MyWeb. Pincha en el siguiente link para restablecer tu contraseña:
mail.forgotpwd.postlink=Este link expirará en 24 horas. Si no deseas modificar tu contraseña, ignora este email y no se realizará ninguna acción.

mail.sign=Equipo MyWeb
