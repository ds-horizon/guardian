from flask import Flask, request, jsonify
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO, format='[%(asctime)s] %(levelname)s: %(message)s')
logger = logging.getLogger(__name__)

# In-memory user store
users = {
    "1": {"name": "John Doe", "email": "john.doe@test.com", "phoneNumber": "777777777", "userId": "1", "username": "user1", "password": "pass1"},
    "2": {"name": "Jane Doe", "email": "jane.doe@test.com", "phoneNumber": "888888888", "userId": "2", "username": "user2", "password": "pass2"}
}

def get_user_by_field(field, value):
    return next((user for user in users.values() if user.get(field) == value), {})

@app.route('/user', methods=['GET'])
def get_user():
    logger.info("GET %s", request.path)
    logger.info("Headers: %s", request.headers)
    logger.info("Query Params: %s", request.args)

    for key in ["phoneNumber", "email", "username"]:
        if key in request.args:
            value = request.args.get(key)
            user = users.get(value) if key == "userId" else get_user_by_field(key, value)
            return jsonify(user), 200

    return jsonify({"error": {"message": "Invalid request"}}), 400

@app.route('/user', methods=['POST'])
def create_user():
    data = request.json
    logger.info("POST /user with data: %s", data)

    username, password = data.get("username"), data.get("password")
    phone, email = data.get("phoneNumber"), data.get("email")

    if not ((username and password) or phone or email):
        return jsonify({"error": {"message": "Invalid request"}}), 400

    for field, message in [("username", "Username taken"), ("phoneNumber", "phone taken"), ("email", "email taken")]:
        if data.get(field) and get_user_by_field(field, data[field]).get("userId"):
            return jsonify({"error": {"message": message}}), 400

    user_id = str(len(users) + 1)
    users[user_id] = {
        "userId": user_id,
        "username": username,
        "password": password,
        "phoneNumber": phone,
        "email": email
    }

    return jsonify(users[user_id]), 201

@app.route('/authenticateUser', methods=['POST'])
def authenticate_user():
    data = request.json
    logger.info("POST /authenticateUser with data: %s", data)

    username, password = data.get("username"), data.get("password")
    if not username or not password:
        return jsonify({"error": {"message": "Invalid request"}}), 400

    user = get_user_by_field("username", username)
    if not user or user.get("password") != password:
        return jsonify({"error": {"message": "User does not exist, Incorrect Password"}}), 400

    return jsonify(user), 200

@app.route('/provider', methods=['POST'])
def post_provider():
    data = request.json
    logger.info("POST /provider with data: %s", data)

    user_id, provider = data.get("userId"), data.get("provider")
    if not user_id or not provider or user_id not in users:
        return jsonify({"error": {"message": "Invalid request"}}), 400

    users[user_id]["provider"] = provider
    return jsonify({}), 200

@app.route('/sendEmail', methods=['POST'])
@app.route('/sendSms', methods=['POST'])
def notify():
    logger.info("POST %s", request.path)
    logger.info("Headers: %s", request.headers)
    logger.info("Payload: %s", request.json)
    return jsonify({}), 200

@app.route('/oidcUser', methods=['GET'])
def get_oidc_user():
    logger.info("GET /oidcUser")
    logger.info("Headers: %s", request.headers)
    logger.info("Query Params: %s", request.args)
    
    user_id = request.args.get("userId")
    scope = request.args.get("scope")
    
    if not user_id or user_id not in users:
        return jsonify({"error": {"message": "User not found"}}), 404
    
    user = users[user_id].copy()
    
    # Filter user data based on scope
    if scope:
        scopes = scope.split()
        filtered_user = {"sub": user_id}
        
        if "email" in scopes:
            filtered_user["email"] = user.get("email")
            filtered_user["email_verified"] = True
            
        if "phone" in scopes:
            filtered_user["phone_number"] = user.get("phoneNumber")
            filtered_user["phone_number_verified"] = True
            
        if "profile" in scopes or "read:profile" in scopes:
            filtered_user["name"] = user.get("name")
            filtered_user["picture"] = "https://example.com/avatar.jpg"
            
        return jsonify(filtered_user), 200
    else:
        # No scopes - return basic info
        return jsonify({"sub": user_id}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=6000, debug=True)
