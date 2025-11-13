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
    echo json_encode(['allowed' => false, 'reason' => 'sensor_no_registrado']);
    exit;
}
$allowed = strtolower($row['status']) === 'activo';
echo json_encode(['allowed' => $allowed, 'user_id' => $row['user_id'], 'status' => $row['status'], 'sensor_id' => $row['id']]);
?>