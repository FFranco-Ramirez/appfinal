<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
require __DIR__ . '/db.php';
function respond($payload, $status = 200) {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}
try {
    $sql = 'SELECT d.id_departamento AS department_id, d.nombre AS nombre
            FROM departamentos d
            LEFT JOIN usuarios u ON u.department_id = d.id_departamento AND u.rol = "ADMIN" AND u.estado = "ACTIVO"
            WHERE u.id_usuario IS NULL
            ORDER BY d.nombre';
    $items = $pdo->query($sql)->fetchAll(PDO::FETCH_ASSOC);
    foreach ($items as &$r) { $r['department_id'] = (int)$r['department_id']; }
    respond(['ok' => true, 'items' => $items]);
} catch (Throwable $e) {
    respond(['ok' => false, 'error' => 'db_error', 'message' => $e->getMessage()], 500);
}
?>
