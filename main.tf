provider "aws" {
  region = "us-east-1"
}

# VPC
resource "aws_vpc" "ku_dondza_vpc" {
  cidr_block = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "ku-dondza-vpc"
  }
}

# Public Subnets
resource "aws_subnet" "ku_dondza_public_subnet_1" {
  vpc_id            = aws_vpc.ku_dondza_vpc.id
  cidr_block        = "10.0.1.0/24"
  availability_zone = "us-east-1a"
  tags = {
    Name = "ku-dondza-public-subnet-1"
  }
}

resource "aws_subnet" "ku_dondza_public_subnet_2" {
  vpc_id            = aws_vpc.ku_dondza_vpc.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = "us-east-1b"
  tags = {
    Name = "ku-dondza-public-subnet-2"
  }
}

# Private Subnets for RDS
resource "aws_subnet" "ku_dondza_private_subnet_1" {
  vpc_id            = aws_vpc.ku_dondza_vpc.id
  cidr_block        = "10.0.3.0/24"
  availability_zone = "us-east-1a"
  tags = {
    Name = "ku-dondza-private-subnet-1"
  }
}

resource "aws_subnet" "ku_dondza_private_subnet_2" {
  vpc_id            = aws_vpc.ku_dondza_vpc.id
  cidr_block        = "10.0.4.0/24"
  availability_zone = "us-east-1b"
  tags = {
    Name = "ku-dondza-private-subnet-2"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "ku_dondza_igw" {
  vpc_id = aws_vpc.ku_dondza_vpc.id
  tags = {
    Name = "ku-dondza-igw"
  }
}

# Public Route Table
resource "aws_route_table" "ku_dondza_public_route_table" {
  vpc_id = aws_vpc.ku_dondza_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.ku_dondza_igw.id
  }

  tags = {
    Name = "ku-dondza-public-route-table"
  }
}

# Route Table Associations
resource "aws_route_table_association" "ku_dondza_public_subnet_association_1" {
  subnet_id      = aws_subnet.ku_dondza_public_subnet_1.id
  route_table_id = aws_route_table.ku_dondza_public_route_table.id
}

resource "aws_route_table_association" "ku_dondza_public_subnet_association_2" {
  subnet_id      = aws_subnet.ku_dondza_public_subnet_2.id
  route_table_id = aws_route_table.ku_dondza_public_route_table.id
}

# Security Group for Web (ECS and Load Balancer)
resource "aws_security_group" "ku_dondza_web_sg" {
  name        = "ku-dondza-web-sg"
  description = "Security group for Ku Dondza web services"
  vpc_id      = aws_vpc.ku_dondza_vpc.id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ku-dondza-web-sg"
  }
}

# RDS Database
variable "db_password" {
  description = "Password for the RDS database (must be at least 8 characters)"
  type        = string
  sensitive   = true
  default     = "fabiocondo123"
}

resource "aws_db_instance" "ku_dondza_db" {
  identifier           = "ku-dondza-db"
  allocated_storage    = 20
  storage_type         = "gp2"
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.t3.micro"
  db_name              = "ku_dondza_db"
  username             = "admin"
  password             = var.db_password
  parameter_group_name = "default.mysql8.0"
  skip_final_snapshot  = true
  vpc_security_group_ids = [aws_security_group.ku_dondza_db_sg.id]
  db_subnet_group_name = aws_db_subnet_group.ku_dondza_db_subnet_group.name
  publicly_accessible  = true  # Permitir acesso público
  tags = {
    Name = "ku-dondza-db"
  }
}

# Security Group for RDS
resource "aws_security_group" "ku_dondza_db_sg" {
  name        = "ku-dondza-db-sg"
  description = "Security group for Ku Dondza RDS"
  vpc_id      = aws_vpc.ku_dondza_vpc.id

  ingress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Permite conexões de qualquer IP (apenas para testes)
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ku-dondza-db-sg"
  }
}

# DB Subnet Group (Usando Subnets Públicas)
resource "aws_db_subnet_group" "ku_dondza_db_subnet_group" {
  name       = "ku-dondza-db-subnet-group"
  subnet_ids = [aws_subnet.ku_dondza_public_subnet_1.id, aws_subnet.ku_dondza_public_subnet_2.id]
  tags = {
    Name = "ku-dondza-db-subnet-group"
  }
}

# ECR Repository
resource "aws_ecr_repository" "ku_dondza_repo" {
  name = "ku-dondza-repo"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "ku-dondza-repo"
  }
}

# ECS Cluster
resource "aws_ecs_cluster" "ku_dondza_cluster" {
  name = "ku-dondza-cluster"
  tags = {
    Name = "ku-dondza-cluster"
  }
}

# CloudWatch Log Group for ECS
resource "aws_cloudwatch_log_group" "ku_dondza_app_logs" {
  name              = "/ecs/ku-dondza-app"
  retention_in_days = 7
  tags = {
    Name = "ku-dondza-app-logs"
  }
}

# ECS Task Definition
resource "aws_ecs_task_definition" "ku_dondza_task" {
  family                   = "ku-dondza-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ku_dondza_ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "ku-dondza-app"
      image     = "${aws_ecr_repository.ku_dondza_repo.repository_url}:latest"
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
          protocol      = "tcp"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.ku_dondza_app_logs.name
          awslogs-region        = "us-east-1"
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])

  tags = {
    Name = "ku-dondza-task"
  }
}

# IAM Role for ECS
resource "aws_iam_role" "ku_dondza_ecs_task_execution_role" {
  name = "ku-dondza-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "ku-dondza-ecs-task-execution-role"
  }
}

resource "aws_iam_role_policy_attachment" "ku_dondza_ecs_task_execution_policy" {
  role       = aws_iam_role.ku_dondza_ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ECS Service
resource "aws_ecs_service" "ku_dondza_service" {
  name            = "ku-dondza-service"
  cluster         = aws_ecs_cluster.ku_dondza_cluster.id
  task_definition = aws_ecs_task_definition.ku_dondza_task.arn
  desired_count   = 2  # Mínimo número inicial de tarefas (tasks)
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = [aws_subnet.ku_dondza_public_subnet_1.id, aws_subnet.ku_dondza_public_subnet_2.id]
    security_groups = [aws_security_group.ku_dondza_web_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.ku_dondza_tg.arn
    container_name   = "ku-dondza-app"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.ku_dondza_listener]

  tags = {
    Name = "ku-dondza-service"
  }
}

# ECS Auto Scaling Target
resource "aws_appautoscaling_target" "ku_dondza_ecs_target" {
  max_capacity       = 4
  min_capacity       = 2  # Mínimo de 2 tarefas
  resource_id        = "service/${aws_ecs_cluster.ku_dondza_cluster.name}/${aws_ecs_service.ku_dondza_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

# ECS Auto Scaling Policy (CPU-based)
resource "aws_appautoscaling_policy" "ku_dondza_ecs_cpu_policy" {
  name               = "ku-dondza-ecs-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ku_dondza_ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ku_dondza_ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ku_dondza_ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
    target_value = 60
  }
}

# ECS Auto Scaling Policy (Memory-based)
resource "aws_appautoscaling_policy" "ku_dondza_ecs_memory_policy" {
  name               = "ku-dondza-ecs-memory-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ku_dondza_ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ku_dondza_ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ku_dondza_ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageMemoryUtilization"
    }
    target_value = 60
  }
}

# Application Load Balancer
resource "aws_lb" "ku_dondza_lb" {
  name               = "ku-dondza-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.ku_dondza_web_sg.id]
  subnets            = [aws_subnet.ku_dondza_public_subnet_1.id, aws_subnet.ku_dondza_public_subnet_2.id]

  tags = {
    Name = "ku-dondza-lb"
  }
}

# ALB Target Group
resource "aws_lb_target_group" "ku_dondza_tg" {
  name     = "ku-dondza-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.ku_dondza_vpc.id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    protocol            = "HTTP"
    port                = "traffic-port"
    healthy_threshold   = 2
    unhealthy_threshold = 2
    timeout             = 3
    interval            = 30
  }

  tags = {
    Name = "ku-dondza-tg"
  }
}

# ALB Listener
resource "aws_lb_listener" "ku_dondza_listener" {
  load_balancer_arn = aws_lb.ku_dondza_lb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.ku_dondza_tg.arn
  }

  tags = {
    Name = "ku-dondza-listener"
  }
}

# Outputs
output "ku_dondza_alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.ku_dondza_lb.dns_name
}

output "ku_dondza_rds_endpoint" {
  description = "Endpoint of the RDS instance"
  value       = aws_db_instance.ku_dondza_db.endpoint
}

output "ku_dondza_ecr_repository_url" {
  description = "URL do repositório ECR"
  value       = aws_ecr_repository.ku_dondza_repo.repository_url
}

output "ku_dondza_ecs_cluster_name" {
  description = "Nome do cluster ECS"
  value       = aws_ecs_cluster.ku_dondza_cluster.name
}