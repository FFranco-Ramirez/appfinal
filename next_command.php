<?php
require __DIR__ . '/db.php';
$deviceId = $_GET['device_id'] ?? null;
if (!$deviceId) {
    http_response_code(400);
    echo json_encode(['error' => 'device_id requerido']);
    exit;
}
$stmt = $pdo->prepare('SELECT id, action FROM barrier_commands WHERE device_id = ? AND status = "queued" ORDER BY id ASC LIMIT 1');
$stmt->execute([$deviceId]);
$cmd = $stmt->fetch();
if (!$cmd) {
    echo json_encode(['command' => null]);
    exit;
}
$pdo->prepare('UPDATE barrier_commands SET status = "dispatched", executed_at = NOW() WHERE id = ?')->execute([$cmd['id']]);
echo json_encode(['command' => $cmd]);
?>