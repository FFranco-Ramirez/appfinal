<?php
require __DIR__ . '/db.php';
$uid = $_GET['uid'] ?? null;
$departmentId = $_GET['department_id'] ?? null;
if (!$uid || !$departmentId) {
    http_response_code(400);
    echo json_encode(['error' => 'uid y department_id requeridos']);
    exit;
}
$stmt = $pdo->prepare('SELECT id, status, user_id FROM sensors WHERE uid = ? AND department_id = ?');
$stmt->execute([$uid, $departmentId]);
$row = $stmt->fetch();
if (!$row) {
    $pdo->prepare('INSERT INTO eventos_acceso (id_sensor, id_usuario, tipo_evento, fecha_hora, resultado) VALUES (0, NULL, "ACCESO_RECHAZADO", NOW(), "DENEGADO")')->execute();
    echo json_encode(['allowed' => false, 'reason' => 'sensor_no_registrado']);
    exit;
}
$allowed = strtolower($row['status']) === 'activo';
if ($allowed) {
    $pdo->prepare('INSERT INTO eventos_acceso (id_sensor, id_usuario, tipo_evento, fecha_hora, resultado) VALUES (?, ?, "ACCESO_VALIDO", NOW(), "PERMITIDO")')->execute([$row['id'], $row['user_id']]);
} else {
    $pdo->prepare('INSERT INTO eventos_acceso (id_sensor, id_usuario, tipo_evento, fecha_hora, resultado) VALUES (?, ?, "ACCESO_RECHAZADO", NOW(), "DENEGADO")')->execute([$row['id'], $row['user_id']]);
}
echo json_encode(['allowed' => $allowed, 'user_id' => $row['user_id'], 'status' => $row['status'], 'sensor_id' => $row['id']]);
?>
