package com.evaluacion.condominios;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiClient {
    private static RequestQueue queue;
    private static String BASE_URL = "http://72.44.58.119/api/";

    public static void init(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context.getApplicationContext());
        }
    }

    public static void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) return;
        BASE_URL = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public interface Callback {
        void onSuccess(JSONObject res);
        void onError(String message);
    }

    private static String parseError(VolleyError error) {
        if (error == null) return "Error desconocido";
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                return new String(error.networkResponse.data);
            } catch (Exception e) {
                return "HTTP " + error.networkResponse.statusCode;
            }
        }
        return error.getMessage() != null ? error.getMessage() : "Fallo de red";
    }

    // Crear sensor (admin)
    public static void createSensor(String uid, int departmentId, int userId, String status, String type, Callback cb) {
        String url = BASE_URL + "create.php";
        JSONObject body = new JSONObject();
        try {
            body.put("uid", uid);
            body.put("department_id", departmentId);
            body.put("user_id", userId);
            body.put("status", status);
            body.put("type", type);
        } catch (JSONException e) {
            cb.onError("JSON inválido: " + e.getMessage());
            return;
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    // Cambiar estado de sensor (admin)
    public static void updateSensorStatus(int sensorId, String status, Callback cb) {
        String url = BASE_URL + "update_status.php";
        JSONObject body = new JSONObject();
        try {
            body.put("sensor_id", sensorId);
            body.put("status", status); // ACTIVO/INACTIVO/PERDIDO/BLOQUEADO
        } catch (JSONException e) {
            cb.onError("JSON inválido: " + e.getMessage());
            return;
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    // Abrir/Cerrar barrera (la API encola comando y registra evento manual)
    public static void barrierControl(int deviceId, int departmentId, String action, int requestedBy, Callback cb) {
        String url = BASE_URL + "control.php";
        JSONObject body = new JSONObject();
        try {
            body.put("device_id", deviceId);
            body.put("department_id", departmentId);
            body.put("action", action); // "open" o "close"
            body.put("requested_by", requestedBy);
        } catch (JSONException e) {
            cb.onError("JSON inválido: " + e.getMessage());
            return;
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    public static void listSensors(int departmentId, Callback cb) {
        String url = BASE_URL + "list.php?department_id=" + departmentId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    public static void listAccessEvents(int departmentId, Integer limit, String tipoEvento, String resultado, Callback cb) {
        StringBuilder url = new StringBuilder(BASE_URL + "access_events_list.php?department_id=" + departmentId);
        if (limit != null) url.append("&limit=").append(limit);
        if (tipoEvento != null && !tipoEvento.isEmpty()) url.append("&tipo_evento=").append(tipoEvento);
        if (resultado != null && !resultado.isEmpty()) url.append("&resultado=").append(resultado);

        JsonObjectRequest req = new JsonObjectRequest(com.android.volley.Request.Method.GET, url.toString(), null,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    // Login real: email + password
    public static void login(String email, String password, Callback cb) {
        String url = BASE_URL + "login.php";
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            cb.onError("JSON inválido: " + e.getMessage());
            return;
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    // Crear usuario: para ADMIN se omite department_id; para OPERADOR se exige
    public static void createUser(String name, String email, String password, String role, int departmentId, int requestedBy, Callback cb) {
        String url = BASE_URL + "user_create.php";
        JSONObject body = new JSONObject();
        try {
            body.put("nombre", name);
            body.put("email", email);
            body.put("password", password);
            body.put("rol", role);
            body.put("requested_by", requestedBy);
            if (!"ADMIN".equalsIgnoreCase(role)) {
                body.put("department_id", departmentId);
            }
        } catch (JSONException e) {
            cb.onError("JSON inválido: " + e.getMessage());
            return;
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    // Departamentos sin ADMIN activo
    public static void listDepartmentsWithoutAdmin(Callback cb) {
        String url = BASE_URL + "departments_without_admin.php";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    // Crear departamento y asociar al ADMIN solicitante
    public static void createDepartment(String nombre, int requestedBy, Callback cb) {
        String url = BASE_URL + "department_create.php";
        JSONObject body = new JSONObject();
        try {
            body.put("nombre", nombre);
            body.put("requested_by", requestedBy);
        } catch (JSONException e) {
            cb.onError("JSON inválido: " + e.getMessage());
            return;
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }

    // Asociar ADMIN a un departamento existente sin ADMIN
    public static void setAdminDepartment(int requestedBy, int departmentId, Callback cb) {
        String url = BASE_URL + "department_set_admin.php";
        JSONObject body = new JSONObject();
        try {
            body.put("requested_by", requestedBy);
            body.put("department_id", departmentId);
        } catch (JSONException e) {
            cb.onError("JSON inválido: " + e.getMessage());
            return;
        }
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                cb::onSuccess,
                error -> cb.onError(parseError(error)));
        queue.add(req);
    }
}
