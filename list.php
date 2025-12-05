<?php
// Lista de sensores por departamento, con filtros opcionales.
// Requiere: db.php (PDO $pdo)
// Respuesta: {"ok":true,"items":[ ... ]} o {"ok":false,"error":"..."}

header('Content-Type: application/json; charset=utf-8');
// Opcional para pruebas desde navegador; la app móvil no necesita CORS:
header('Access-Control-Allow-Origin: *');

require __DIR__ . '/db.php';

function respond($payload, $status = 200) {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

// Parámetros
$departmentId = isset($_GET['department_id']) ? trim($_GET['department_id']) : null;
if (!$departmentId || !ctype_digit($departmentId)) {
    respond(['ok' => false, 'error' => 'department_id requerido y debe ser numérico'], 400);
}

$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
if ($limit < 1) $limit = 1;
if ($limit > 500) $limit = 500;

$status = isset($_GET['status']) ? strtoupper(trim($_GET['status'])) : null;
$allowedStatus = ['ACTIVO','INACTIVO','PERDIDO','BLOQUEADO'];
if ($status && !in_array($status, $allowedStatus)) $status = null;

$tipo = isset($_GET['tipo']) ? strtoupper(trim($_GET['tipo'])) : null;
$allowedTipo = ['LLAVERO','TARJETA'];
if ($tipo && !in_array($tipo, $allowedTipo)) $tipo = null;

$search = isset($_GET['search']) ? trim($_GET['search']) : null;

$sql = 'SELECT 
            id_sensor,
            codigo_sensor,
            estado,
            tipo,
            id_departamento,
            id_usuario,
            fecha_alta,
            fecha_baja
        FROM sensores
        WHERE id_departamento = ?';

$params = [$departmentId];

if ($status) {
    $sql .= ' AND estado = ?';
    $params[] = $status;
}
if ($tipo) {
    $sql .= ' AND tipo = ?';
    $params[] = $tipo;
}
if ($search) {
    $sql .= ' AND (codigo_sensor LIKE ?)';
    $like = '%' . $search . '%';
    $params[] = $like;
}

// Evitar placeholder en LIMIT para máxima compatibilidad PDO/MySQL
$sql .= ' ORDER BY fecha_alta DESC LIMIT ' . (int)$limit;

try {
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    respond(['ok' => true, 'items' => $items]);
} catch (Throwable $e) {
    respond(['ok' => false, 'error' => 'db_error', 'message' => $e->getMessage()], 500);
}
?>
