# Инструкция  

- Необходимо находится в папке FCM_python
- Установить venv `python3 -m venv venv` 
- Запустить venv `source venv/bin/activate`
- Установить зависимости `pip install -r requirements.txt`
- Добавить голый ключ FCM в файл user_token 
- Положить файл service-account.json или добавить серверный ключ FCM в файл fcm_project_token 
- Запустить `python3 message.py` или `python3 message2.py` если токен в файле fcm_project_token

Отключить venv `deactivate`