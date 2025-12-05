<?php
// Lista de eventos de acceso por departamento (historial para admin y operadores).
// Requiere: db.php (PDO $pdo)
// Respuesta: {"ok":true,"items":[ ... ]} o {"ok":false,"error":"..."}

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

require __DIR__ . '/db.php';

function respond($payload, $status = 200) {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

// department_id es obligatorio, pero en la tabla EVENTOS_ACCESO que describiste
// no existe explícitamente; se filtrará por el departamento del sensor (JOIN sensores).
$departmentId = isset($_GET['department_id']) ? trim($_GET['department_id']) : null;
if (!$departmentId || !ctype_digit($departmentId)) {
    respond(['ok' => false, 'error' => 'department_id requerido y numérico'], 400);
}

$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
if ($limit < 1) $limit = 1;
if ($limit > 500) $limit = 500;

$sensorId = isset($_GET['id_sensor']) ? trim($_GET['id_sensor']) : null;
if ($sensorId && !ctype_digit($sensorId)) $sensorId = null;

$tipoEvento = isset($_GET['tipo_evento']) ? strtoupper(trim($_GET['tipo_evento'])) : null;
$allowedTipos = ['ACCESO_VALIDO','ACCESO_RECHAZADO','APERTURA_MANUAL','CIERRE_MANUAL'];
if ($tipoEvento && !in_array($tipoEvento, $allowedTipos)) $tipoEvento = null;

$resultado = isset($_GET['resultado']) ? strtoupper(trim($_GET['resultado'])) : null;
$allowedResultados = ['PERMITIDO','DENEGADO'];
if ($resultado && !in_array($resultado, $allowedResultados)) $resultado = null;

$from = isset($_GET['from']) ? trim($_GET['from']) : null; // 'YYYY-MM-DD' o 'YYYY-MM-DD HH:MM:SS'
$to   = isset($_GET['to'])   ? trim($_GET['to'])   : null;

$sql = 'SELECT 
            ea.id_evento,
            ea.id_sensor,
            ea.id_usuario,
            ea.tipo_evento,
            ea.fecha_hora,
            ea.resultado,
            s.codigo_sensor,
            s.tipo AS sensor_tipo,
            s.id_departamento,
            u.nombre AS usuario_nombre
        FROM eventos_acceso ea
        LEFT JOIN sensores s ON s.id_sensor = ea.id_sensor
        LEFT JOIN usuarios u ON u.id_usuario = ea.id_usuario
        WHERE (s.id_departamento = ? OR (ea.tipo_evento IN ("APERTURA_MANUAL","CIERRE_MANUAL") AND u.department_id = ?))';

$params = [$departmentId, $departmentId];

if ($sensorId) {
    $sql .= ' AND ea.id_sensor = ?';
    $params[] = $sensorId;
}
if ($tipoEvento) {
    $sql .= ' AND ea.tipo_evento = ?';
    $params[] = $tipoEvento;
}
if ($resultado) {
    $sql .= ' AND ea.resultado = ?';
    $params[] = $resultado;
}
if ($from) {
    $sql .= ' AND ea.fecha_hora >= ?';
    $params[] = $from;
}
if ($to) {
    $sql .= ' AND ea.fecha_hora <= ?';
    $params[] = $to;
}

$sql .= ' ORDER BY ea.fecha_hora DESC LIMIT ' . (int)$limit;

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);
    respond(['ok' => true, 'items' => $items]);
} catch (Throwable $e) {
    respond(['ok' => false, 'error' => 'db_error', 'message' => $e->getMessage()], 500);
}
?>
