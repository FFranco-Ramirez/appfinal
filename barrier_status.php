<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
require __DIR__ . '/db.php';
function respond($p,$s=200){ http_response_code($s); echo json_encode($p, JSON_UNESCAPED_UNICODE); exit; }

$departmentId = isset($_GET['department_id']) ? (int)$_GET['department_id'] : 0;
if ($departmentId <= 0) respond(['ok'=>false,'error'=>'department_id requerido y numérico'],400);

try {
    $sql = 'SELECT ea.tipo_evento, ea.fecha_hora
            FROM eventos_acceso ea
            JOIN usuarios u ON u.id_usuario = ea.id_usuario
            WHERE u.department_id = ? AND ea.tipo_evento IN ("APERTURA_MANUAL","CIERRE_MANUAL")
            ORDER BY ea.fecha_hora DESC LIMIT 1';
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$departmentId]);
    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$row) respond(['ok'=>true, 'status'=>'UNKNOWN']);

    $status = $row['tipo_evento'] === 'APERTURA_MANUAL' ? 'OPEN' : 'CLOSED';
    respond(['ok'=>true, 'status'=>$status, 'last_event'=>$row]);
} catch (Throwable $e) {
    respond(['ok'=>false,'error'=>'db_error','message'=>$e->getMessage()],500);
}
?>
